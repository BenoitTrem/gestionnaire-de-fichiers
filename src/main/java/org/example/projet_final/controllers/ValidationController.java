package org.example.projet_final.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.services.JWTUtilsService;
import org.example.projet_final.services.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class ValidationController {
    private final JWTUtilsService jwtUtilsService;
    private final UtilisateurService utilisateurService;
    private final PasswordEncoder passwordEncoder;
    public ValidationController(JWTUtilsService jwtUtilsService, UtilisateurService utilisateurService, PasswordEncoder passwordEncoder) {
        this.jwtUtilsService = jwtUtilsService;
        this.utilisateurService = utilisateurService;
        this.passwordEncoder = passwordEncoder;

    }

    /**
     * @author Bruno
     *
     *  Valide l'email d'un utilisateur en utilisant un token de validation.
     *  Le token est validé pour extraire l'email de l'utilisateur, puis l'utilisateur correspondant est récupéré.
     *  Si l'utilisateur est trouvé, son statut `estVirifie` est mis à `true`.
     *
     * @param token Le token de validation envoyé à l'utilisateur.
     * @param request L'objet `HttpServletRequest` qui peut contenir des informations supplémentaires sur la requête.
     * @return Une réponse HTTP indiquant si l'email a été validé avec succès ou non.
     */
    @GetMapping("/valider")
    public ResponseEntity<String> validerEmail(@RequestParam("token") String token,
                                               HttpServletRequest request) {
        try {
            String email = jwtUtilsService.validerJetonEtExtraireCourriel(token);
            Utilisateur utilisateur = utilisateurService.getByEmail(email);
            if (utilisateur == null) {
                return ResponseEntity.ok().body("Utilisateur n'existe pas");
            }

            utilisateur.setEstVirifie(true);
            utilisateurService.sauvegarder(utilisateur);

            return ResponseEntity.ok().body("Email validé");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Erreur lors de la validation du email");
        }
    }




    /**
     * @author John
     *
     *  Envoie un email de réinitialisation du mot de passe à un utilisateur.
     *  Le système génère un token de réinitialisation et l'envoie à l'email fourni.
     *
     * @param email L'email de l'utilisateur qui a oublié son mot de passe.
     * @return Une réponse HTTP indiquant si l'email a été envoyé avec succès ou non.
     */
    @PostMapping("/MotDePasse")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        try {
            utilisateurService.envoyerCourrielPasswordoublier(email);
            return ResponseEntity.ok("Email envoyé pour la réinitialisation du mot de passe");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de l'envoi du courriel");
        }
    }



    /**
     * @author Benoit
     *
     * Réinitialise le mot de passe d’un utilisateur si un token JWT est valide.
     * @param token token Jeton envoyé par courriel pour vérifier l'identité
     * @return Page HTML affichant le nouveau mot de passe
     */
    @GetMapping("/motdepasseRT")
    public ResponseEntity<String> resetPasswordViaToken(@RequestParam("token") String token) {
        try {
            // Vérifie le token et récupère le courriel qu'il contient.
            String email = jwtUtilsService.validerJetonEtExtraireCourriel(token);
            Utilisateur u = utilisateurService.getByEmail(email);

            // Vérification si l'utilisateur est introuvable.
            if (u == null) {
                return ResponseEntity
                        .badRequest()
                        .header("Content-Type", "text/html; charset=UTF-8")
                        .body("<p style='text-align:center; color:red;'>Erreur : utilisateur introuvable.</p>");
            }

            // Génère un nouveau mot de passe temporaire.
            String randomPassword = genererRandomMotDePasse(12);

            u.setPassword(passwordEncoder.encode(randomPassword)); // Met à jour et sauvegarde le mot de passe de l'utilisateur.
            utilisateurService.sauvegarder(u);

            // HTML affiché après la réinitialisation.
            String htmlMessage = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Mot de passe réinitialisé</title>
                    <style>
                            body {
                                font-family: Arial, sans-serif;
                                text-align: center;
                                padding: 50px;
                                background-color: #495057;
                                color: black;
                            }
                            .container_ {
                                background-color: white;
                                border: 1px solid black;
                                box-shadow: 0px 4px 10px rgba(0, 0, 0, 0.8);
                                border-radius: 10px;
                                padding: 30px;
                                max-width: 500px;
                                margin: 0 auto;
                                transition: all 0.3s ease;
                            }
                            .container_:hover {
                                box-shadow: 0px 6px 15px rgba(0, 0, 0, 0.8);
                            }
                            .password {
                                font-size: 1.5em;
                                font-weight: bold;
                                color: white;
                                background-color: #343a40;
                                padding: 15px 30px;
                                border-radius: 5px;
                                display: inline-block;
                                margin: 20px 0;
                                transition: all 0.3s ease;
                            }
                            .password:hover {
                              background-color: white;
                              color: black;
                            }
                            h2 {
                                color: black;
                                font-weight: bold;
                                margin-bottom: 20px;
                            }
                            p {
                                font-size: 1em;
                                color: #343a40;
                            }
                        </style>
                </head>
                    <body>
                        <div class="container_">
                            <h2>Mot de passe réinitialisé avec succès !</h2>
                            <p>Voici votre nouveau mot de passe temporaire :</p>
                            <div class="password">%s</div>
                            <p>
                                Veuillez le copier et vous connecter avec ce mot de passe.<br>
                                Une fois connecté, vous pourrez le changer dans votre compte.
                            </p>
                        </div>
                    </body>
                </html>
                """.formatted(randomPassword);

            return ResponseEntity
                    .ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body(htmlMessage);

        } catch (Exception ex) {
            return ResponseEntity
                    .badRequest()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .body("<p style='text-align:center; color:red;'>Erreur : token invalide ou expiré.</p>");
        }
    }

    /**
     * @author Benoit
     *
     * Génère un mot de passe aléatoire.
     * @param longueur Longueur du mot de passe souhaité
     * @return  Un mot de passe généré aléatoirement
     */
    private String genererRandomMotDePasse(int longueur ) {
        // Ensemble des caractères possibles pour générer le mot de passe.
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();

        // Boucle pour ajouter un caractère aléatoire à chaque boucle.
        for (int i = 0; i < longueur ; i++) {
            int idx = (int) (Math.random() * chars.length());
            password.append(chars.charAt(idx));
        }
        return password.toString();
    }

    /**
     * @author John
     *
     *  Permet à un utilisateur de réinitialiser son mot de passe en utilisant un token de réinitialisation.
     *  Le token est validé pour extraire l'email de l'utilisateur, puis l'utilisateur correspondant est récupéré.
     *  Si l'utilisateur est trouvé, son mot de passe est modifié avec celui envoyé dans le corps de la requête.
     *
     * @param token Le token de réinitialisation envoyé à l'utilisateur.
     * @param body Un `Map` contenant le nouveau mot de passe dans le champ "password".
     * @return Une réponse HTTP indiquant si la réinitialisation a réussi ou échoué.
     */
    @PostMapping("/motdepasseRT/reset")
    public ResponseEntity<String> resetPassword(
            @RequestParam("token") String token,
            @RequestBody Map<String, String> body) {

        try {
            String email = jwtUtilsService.validerJetonEtExtraireCourriel(token);
            Utilisateur u = utilisateurService.getByEmail(email);

            if (u == null) {
                return ResponseEntity.badRequest().body("Utilisateur introuvable");
            }

            String nouveauMDP = body.get("password");
            u.setPassword(passwordEncoder.encode(nouveauMDP));
            utilisateurService.sauvegarder(u);

            return ResponseEntity.ok("Mot de passe changé avec succès");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Impossible de changer le mot de passe");
        }
    }


}

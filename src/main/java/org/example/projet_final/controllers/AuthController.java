package org.example.projet_final.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.example.projet_final.models.Role;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.auth.AuthResponse;
import org.example.projet_final.models.auth.LoginRequest;
import org.example.projet_final.models.auth.RefreshToken;
import org.example.projet_final.models.auth.RegisterRequest;
import org.example.projet_final.services.FileService;
import org.example.projet_final.services.TokenService;
import org.example.projet_final.services.UtilisateurService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UtilisateurService  utilisateurService;
    private final FileService fileService;


    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, UtilisateurService utilisateurService, FileService fileService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.utilisateurService = utilisateurService;
        this.fileService = fileService;
    }

    /**
     * @author Benoit
     *
     * Point de connexion d'un utilisateur.
     * Authentifie l’email et le mot de passe, puis génère un JWT et un refresh token.
     * @param request Données de connexion (email + mot de passe)
     * @param response Réponse HTTP permettant d'ajouter le cookie du refresh token
     * @return AuthResponse contenant le JWT et les infos de l’utilisateur
     */
    @PostMapping("/connexion")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            // Authentification avec Spring Security.
            Authentication authentication;
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            String email = authentication.getName();
            Utilisateur utilisateur = utilisateurService.getByEmail(email);

            if (utilisateur == null) {
                return ResponseEntity.status(401)
                        .body(new AuthResponse(null, "Bearer", email, null, "Courriel ou mot de passe invalide.", null));
            }

            // Vérifie que le courriel de l'utilisateur est vérifié.
            if (!utilisateur.isEstVirifie()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new AuthResponse(null, "Bearer", email, null, "Courriel non virifiée.", null));
            }

            // Génération du JWT.
            String token = tokenService.generateToken(authentication);

            // Création du refresh token et stockage en BDD.
            RefreshToken refreshToken = tokenService.creerRefreshToken(utilisateur.getId());

            // Création du Cookie HTTP-Only contenant le refresh token.
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken.getToken())
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .build();

            System.out.println("Set-Cookie: " + cookie.toString());

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            // Récupération du rôle de l'utilisateur.
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(new AuthResponse(token, "Bearer", email, roles, "Connexion réussie.", utilisateur.getId()));

        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new AuthResponse(null, "Bearer", request.getEmail(), null, "Courriel invalide ou mot de passe invalide.", null));
        }
    }

    /**
     * @author Benoit
     *
     * Méthode d'inscription d'un nouvel utilisateur.
     *
     * @param username Nom d'utilisateur choisi
     * @param email  Adresse courriel de l'utilisateur
     * @param password Mot de passe
     * @param confirmerPassword Confirmation du mot de passe
     * @param profileImage  Image de profil (optionnelle)
     * @return  Message indiquant le résultat de l'inscription
     */
    @PostMapping("/inscription")
    public ResponseEntity<String> register(@RequestParam("username") String username, @RequestParam("email") String email,
                                           @RequestParam("password") String password, @RequestParam("confirmerPassword") String confirmerPassword,
                                           @RequestParam(value = "profileImage", required = false) MultipartFile profileImage) {

        // Vérifie que les mots de passe correspondent.
        if (!password.equals(confirmerPassword)) {
            return ResponseEntity.badRequest().body("Les mots de passe ne correspondent pas.");
        }

        // Vérifie si l'email existe déjà.
        if (utilisateurService.getByEmail(email) != null) {
            return ResponseEntity.badRequest().body("Ce courriel existe déjà.");
        }

        // Vérifie si le nom d'utilisateur existe déjà.
        if (utilisateurService.getByUsername(username) != null) {
            return ResponseEntity.badRequest().body("Ce nom d'utilisateur existe déjà.");
        }

        try {
            // Création d'un objet RegisterRequest pour passer les informations.
            RegisterRequest request = new RegisterRequest(username, email, password, confirmerPassword);

            // Création de l'utilisateur.
            Utilisateur nouvelUtilisateur = utilisateurService.ajouter(request, profileImage);

            // Retourne un message de succès
            return ResponseEntity.ok("Inscription réussie ! Vérifiez votre e-mail.");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'inscription : " + e.getMessage());
        }
    }


    /**
     * @author Benoit
     *
     * Point d'entrée pour rafraîchir le JWT à partir du Refresh Token.
     *
     * @param token Refresh token récupéré dans le cookie
     * @param response Réponse HTTP (permet d'ajouter le nouveau cookie)
     * @return  Nouveau JWT + nouveau refresh token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue("refreshToken") String token, HttpServletResponse response) {
        // Affiche le refresh token reçu.

        System.out.println("Token reçu: " + token);

        RefreshToken refreshToken;
        try {
            // Valide le refresh token envoyé par le client.
            refreshToken = tokenService.validerRefreshToken(token);

            // Génère un nouveau JWT basé sur l'utilisateur lié au refresh token.
            String nouveauJwtToken = tokenService.generateToken(
                    new UsernamePasswordAuthenticationToken(
                            refreshToken.getUtilisateur().getEmail(),
                            null
                    )
            );
            // Récupère l'utilisateur associé.
            Utilisateur utilisateur = utilisateurService.getByEmail(refreshToken.getUtilisateur().getEmail());

            // Crée un nouveau refresh token pour remplacer l'ancien.
            RefreshToken nouveauRefreshToken = tokenService.creerRefreshToken(utilisateur.getId());

            // Récupère le rôle de l'utilisateur.
            Set<String> roles = new HashSet<>();
            for (Role role : utilisateur.getRoles()) {
                roles.add(role.getName());
            }

            // Création du nouveau refresh token.
            ResponseCookie cookie = ResponseCookie.from("refreshToken", nouveauRefreshToken.getToken())
                    .httpOnly(true)
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .secure(false)
                    .build();

            System.out.println("token supprimé: " + refreshToken);

            tokenService.deleteToken(refreshToken);  // Supprime l'ancien refresh token.

            System.out.println("nouveau token: " + nouveauJwtToken);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString()); // Ajoute le nouveau cookie à la réponse HTTP.

            return ResponseEntity.ok(new AuthResponse(
                    nouveauJwtToken,
                    utilisateur.getEmail(),
                    roles,
                    "Token rafraîchi avec succès.",
                    utilisateur.getId()
            ));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("RefreshToken invalide ou expiré.");
        }
    }
}

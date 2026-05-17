package org.example.projet_final.controllers;

import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.dto.PhotoProfilDTO;
import org.example.projet_final.models.dto.UtilisateurDTO;
import org.example.projet_final.services.UtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UtilisateurController {
    private final UtilisateurService utilisateurService;
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    /**
     * @author Benoit
     *
     * Retourne les détails de l'utilisateur actuellement connecté.
     *
     * @return Informations de l'utilisateur sous forme de DTO
     */
    @GetMapping("/utilisateur/details")
    public ResponseEntity<UtilisateurDTO> getUtilisateurDetails() {
        try {
            // Récupère l'utilisateur authentifié
            Utilisateur utilisateur = utilisateurService.getUtilisateur();

            PhotoProfilDTO photoDto = null;

            // Si l'utilisateur possède une photo de profil, ceci construit son DTO.
            if (utilisateur.getPhotoProfile() != null) {
                String photoUrl = "/files/" + utilisateur.getPhotoProfile().getId();
                photoDto = new PhotoProfilDTO(
                        utilisateur.getPhotoProfile().getNomOriginal(),
                        photoUrl,
                        utilisateur.getPhotoProfile().getMimeType()
                );
            }

            // Création du DTO utilisateur avec ID, nom, email, rôles, et photo
            UtilisateurDTO dto = new UtilisateurDTO(
                    utilisateur.getId(),
                    utilisateur.getUsername(),
                    utilisateur.getEmail(),
                    utilisateur.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                    photoDto
            );

            // Retourne les détails
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * @author Benoit
     *
     * Supprime un utilisateur selon son ID.
     * Vérifie que l'utilisateur connecté supprime bien SON propre compte.
     * Retourne un message selon le résultat.
     * @param id ID de l'utilisateur à supprimer
     * @return Réponse HTTP avec message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> supprimerUtilisateur(@PathVariable Integer id) {
        try {
            // Récupère l'utilisateur actuellement connecté
            Utilisateur utilisateur = utilisateurService.getUtilisateur();

            // Vérifie que l'utilisateur ne tente pas de supprimer un autre compte
            if (!utilisateur.getId().equals(id)){
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message", "Vous ne pouvez pas supprimer un compte qui ne vous appartient pas."));
            }

            // Tente de supprimer l'utilisateur
            boolean supprime = utilisateurService.supprimerUtilisateur(id);

            // Si succès
            if (supprime) {
                return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès."));
            } else {
                // Si l’utilisateur n’existe pas
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Utilisateur non trouvé."));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la suppression de l'utilisateur : " + e.getMessage()));
        }
    }

    /**
     * @author Benoit
     *
     * Met à jour les informations d'un utilisateur.
     * Vérifie l'utilisateur connecté
     * Permet de modifier l'email, le mot de passe ou la photo de profil
     *
     * @param id ID de l'utilisateur à modifier
     * @param email Nouveau courriel (optionnel)
     * @param password Nouveau mot de passe (optionnel)
     * @param photoProfile Nouvelle photo de profil (optionnelle)
     * @return Message de confirmation ou d'erreur
     */
    @PostMapping("/utilisateur/{id}")
    public ResponseEntity<Map<String, String>> modifierUtilisateur(@PathVariable Integer id,
                                                                   @RequestParam(required = false) String email,
                                                                   @RequestParam(required = false) String password,
                                                                   @RequestParam(required = false) MultipartFile photoProfile) {
        try {
            // Récupère l'utilisateur connecté.
            Utilisateur utilisateur = utilisateurService.getUtilisateur();

            // Appelle le service pour modifier le profil.
            utilisateurService.modifierUtilisateur(id, email, password, photoProfile, utilisateur);

            return ResponseEntity.ok(Map.of("message", "Profil mis à jour avec succès."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erreur lors de la mise à jour du profil: " + e.getMessage()));
        }
    }

    /**
     * @author Benoit
     *
     * Récupère la liste de tous les utilisateurs, excepté l'utilisateur connecté.
     * Accessible uniquement aux administrateurs.
     * @return Liste des utilisateurs avec leurs rôles et leurs photos de profil
     */
    @GetMapping("/utilisateurs")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UtilisateurDTO>> getUtilisateurAvecRole() {
        // Récupère l'utilisateur actuellement connecté.
        Utilisateur utilisateur = utilisateurService.getUtilisateur();

        // Récupère tous les utilisateurs sauf celui connecté.
        List<Utilisateur> utilisateurs = utilisateurService.getAllUsers().stream()
                .filter(u -> !u.getId().equals(utilisateur.getId()))
                .collect(Collectors.toList());

        // Convertit chaque utilisateur en DTO.
        List<UtilisateurDTO> dtos = utilisateurs.stream().map(u -> {
            // Prépare les informations de la photo de profil si disponible.
            PhotoProfilDTO photoDto = null;
            if (u.getPhotoProfile() != null) {
                String photoUrl = "/files/" + u.getPhotoProfile().getId();
                photoDto = new PhotoProfilDTO(
                        u.getPhotoProfile().getNomOriginal(),
                        photoUrl,
                        u.getPhotoProfile().getMimeType()
                );
            }

            // Retourne un DTO complet de l'utilisateur
            return new UtilisateurDTO(
                    u.getId(),
                    u.getUsername(),
                    u.getEmail(),
                    u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()),
                    photoDto
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * @author Benoit
     *
     * Modifie le rôle d'un utilisateur.
     * Accessible uniquement par un administrateur.
     *
     * @param id  ID de l'utilisateur dont le admin veut modifier le rôle
     * @param nouveauRole Nouveau rôle à appliquer
     * @return Message de succès ou d'erreur
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<Map<String, String>> modifierRoleUtilisateur(@PathVariable Integer id,
                                                                       @RequestParam String nouveauRole) {
        // Appelle le service pour modifier le rôle.
        String message = utilisateurService.modifierRoleUtilisateur(id, nouveauRole);

        // Prépare la réponse à renvoyer au client.
        Map<String, String> response = new HashMap<>();
        response.put("message", message != null ? message : "Erreur");

        // Si la modification a été effectuée avec succès.
        if ("Role modifié".equals(message)) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}


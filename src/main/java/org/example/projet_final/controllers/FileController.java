package org.example.projet_final.controllers;

import org.example.projet_final.models.Commentaire;
import org.example.projet_final.models.Requests.CommentaireRequest;
import org.example.projet_final.models.Requests.ContributorRequest;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.dto.CommentaireDTO;
import org.example.projet_final.models.dto.FichierDTO;
import org.example.projet_final.services.CommentairesService;
import org.example.projet_final.services.UtilisateurService;
import org.springframework.core.io.Resource;
import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.services.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileService fileService;
    private final UtilisateurService utilisateurService;
    private final CommentairesService commentairesService;

    public FileController(FileService fileService,UtilisateurService utilisateurService,CommentairesService commentairesService) {
        this.fileService = fileService;
        this.utilisateurService = utilisateurService;
        this.commentairesService = commentairesService;
    }
    /**
     * @author Bruno
     *
     *  Permet à un utilisateur de télécharger un fichier sur le serveur.
     *  Le fichier est traité et ses métadonnées sont enregistrées.
     *
     * @param file Le fichier à télécharger.
     * @return Une réponse HTTP indiquant si le fichier a été téléchargé avec succès, avec les métadonnées du fichier.
     * @throws IOException En cas d'erreur lors du traitement du fichier.
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            Utilisateur utilisateur = utilisateurService.getUtilisateur();
            FileMetaData metadata = fileService.uploadFile(file, utilisateur);

            return ResponseEntity.ok().body(metadata);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * @author Bruno
     *
     *  Permet de récupérer un fichier en fonction de son ID.
     *  Le fichier est renvoyé sous forme de ressource.
     *
     * @param idfichier L'ID du fichier à récupérer.
     * @return Une réponse HTTP contenant la ressource du fichier.
     * @throws IOException En cas d'erreur lors de la récupération du fichier.
     */
    @GetMapping("/{idfichier}")
    public ResponseEntity<Resource> getFile(@PathVariable Long idfichier) {
        try {
            FileMetaData fileMetaData = fileService.getFileMetadata(idfichier);
            Resource resource = fileService.getFile(idfichier);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileMetaData.getNomOriginal() + "\"")
                    .contentType(MediaType.parseMediaType(fileMetaData.getMimeType()))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * @author John
     *
     *  Permet de récupérer tous les fichiers sous forme de DTO.
     *
     * @return Une liste de DTO représentant tous les fichiers disponibles.
     */
    @GetMapping("/dto")
    public List<FichierDTO> getAllDto() {
        return fileService.getAllDto();
    }

    /**
     * @author John
     *
     *  Permet de récupérer les fichiers de l'utilisateur courant.
     *
     * @return Une liste de DTO représentant les fichiers de l'utilisateur courant.
     */
    @GetMapping("/mesfichiers")
    public List<FichierDTO> getMesFichiers() {
        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        return fileService.getFilesByAuteur(utilisateur);
    }

    /**
     * @author John
     *
     *  Permet à un utilisateur de commenter un fichier.
     *  Un commentaire est ajouté au fichier, et les données du fichier sont retournées.
     *
     * @param request Les informations du commentaire à ajouter.
     * @return Le DTO du fichier mis à jour après l'ajout du commentaire.
     */
    @PostMapping("/commenter")
    public FichierDTO commenter(@RequestBody CommentaireRequest request) {
        Utilisateur auteur = utilisateurService.getUtilisateur();
        return fileService.commenter(request, auteur);
    }

    /**
     * @author John
     *
     *  Permet de signaler un commentaire.
     *  Le commentaire est marqué comme signalé dans la base de données.
     *
     * @param id L'ID du commentaire à signaler.
     * @return Une réponse HTTP indiquant si le commentaire a été signalé avec succès.
     */
    @PostMapping("/SignalerCommentaire/{id}")
    public ResponseEntity<String> signalerCommentaire(@PathVariable Long id) {
        try {
            commentairesService.Signaler(id);
            return ResponseEntity.ok("Commentaire Signalé");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors de la signalisation du commentaire");
        }
    }

    /**
     * @author John
     *
     *  Permet d'ajouter un contributeur à un fichier.
     *  Un utilisateur est ajouté comme contributeur à un fichier.
     *
     * @param id L'ID du fichier auquel ajouter un contributeur.
     * @param request Contient l'email du contributeur à ajouter.
     * @return Une réponse HTTP indiquant que le contributeur a été ajouté avec succès.
     */
    @PostMapping("/{id}/contributeurs")
    public ResponseEntity<?> addContributor(
            @PathVariable Long id,
            @RequestBody ContributorRequest request
    ) {
        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        Utilisateur contributor =  utilisateurService.getByEmail(request.getContributorEmail());

        fileService.addContributor(id, utilisateur.getEmail(), contributor);

        return ResponseEntity.ok("Contributeur ajouté avec succès");
    }

    /**
     * @author John
     *
     *  Permet de retirer un contributeur d'un fichier.
     *  Un utilisateur est retiré de la liste des contributeurs d'un fichier.
     *
     * @param id L'ID du fichier dont retirer un contributeur.
     * @param request Contient l'email du contributeur à retirer.
     * @return Une réponse HTTP indiquant que le contributeur a été retiré avec succès.
     */
    @DeleteMapping("/{id}/contributeurs")
    public ResponseEntity<?> removeContributor(
            @PathVariable Long id,
            @RequestBody ContributorRequest request
    ) {

        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        Utilisateur contributor =  utilisateurService.getByEmail(request.getContributorEmail());

        fileService.removeContributor(id, utilisateur.getEmail(), contributor);
        return ResponseEntity.ok("Contributeur retiré");
    }

    /**
     * @author John
     *
     *  Permet de récupérer la liste des contributeurs d'un fichier.
     *
     * @param id L'ID du fichier dont récupérer la liste des contributeurs.
     * @return Une réponse HTTP contenant la liste des contributeurs sous forme de DTO.
     */
    @GetMapping("/{id}/contributeurs")
    public ResponseEntity<?> listContributors(
            @PathVariable Long id
    ) {
        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        Set<Utilisateur> liste = fileService.listContributors(id, utilisateur.getEmail());
        return ResponseEntity.ok(
                liste.stream().map(utilisateurService::mapToDTO).toList()
        );
    }

    /**
     * @author John
     *
     *  Permet de récupérer les contributeurs disponibles pour un fichier.
     *  Les contributeurs déjà existants sont exclus.
     *
     * @param id L'ID du fichier pour lequel récupérer les contributeurs disponibles.
     * @return Une réponse HTTP contenant la liste des contributeurs disponibles sous forme de DTO.
     */
    @GetMapping("/{id}/utilisateurs-disponibles")
    public ResponseEntity<?> getAvailableContributors(@PathVariable Long id) {

        Utilisateur utilisateurCourant = utilisateurService.getUtilisateur();
        Set<Utilisateur> disponibles = fileService.getAvailableContributors(id, utilisateurCourant);

        return ResponseEntity.ok(
                disponibles.stream().map(utilisateurService::mapToDTO).toList()
        );
    }

    /**
     * @author John
     *
     *  Permet de changer la visibilité d'un fichier.
     *  La visibilité du fichier est inversée : si elle est privée, elle devient publique et vice-versa.
     *
     * @param id L'ID du fichier dont changer la visibilité.
     * @return Une réponse HTTP indiquant si le fichier a été rendu visible ou privé.
     */
    @PostMapping("/{id}/visibilite")
    public ResponseEntity<?> toggleVisibility(
            @PathVariable Long id
    ) {
        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        boolean newState = fileService.changerVisibilite(id, utilisateur.getEmail());
        return ResponseEntity.ok(
                newState ? "Fichier rendu visible" : "Fichier rendu privé"
        );
    }

    /**
     * @author John
     *
     *  Permet d'activer ou désactiver les commentaires sur un fichier.
     *  L'état de l'activation des commentaires est inversé.
     *
     * @param id L'ID du fichier pour lequel activer ou désactiver les commentaires.
     * @return Une réponse HTTP indiquant si les commentaires ont été activés ou désactivés.
     */
    @PostMapping("/{id}/commentaires")
    public ResponseEntity<?> toggleCommentaires(@PathVariable Long id) {
        Utilisateur utilisateur = utilisateurService.getUtilisateur();
        boolean newState = fileService.toggleCommentaires(id, utilisateur.getEmail());

        return ResponseEntity.ok(
                newState ? "Commentaires activés" : "Commentaires désactivés"
        );
    }

    /**
     * @author John
     *
     *  Permet de supprimer un fichier.
     *  Le fichier est supprimé de la base de données et du système de fichiers.
     *
     * @param id L'ID du fichier à supprimer.
     * @return Une réponse HTTP indiquant que le fichier a été supprimé avec succès.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        fileService.deleteFile(id);
        return ResponseEntity.ok("Fichier supprimé");
    }


    /**
     * @author Benoit
     *
     * Récupère la liste des commentaires signalés.
     * Accessible uniquement aux administrateurs.
     *
     * @return Liste de CommentaireDTO
     */
    @GetMapping("/commentaires/signales")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<CommentaireDTO>> getCommentairesSignales() {
        try {
            // Récupère tous les commentaires signalés dans la BD.
            List<Commentaire> commentairesSignales = commentairesService.getCommentairesSignales();

            // Convertit les entités Commentaire en objets CommentaireDTO.
            List<CommentaireDTO> commentaireDTOList = commentairesSignales.stream()
                    .map(commentaire -> {
                        return CommentaireDTO.builder()
                                .id(commentaire.getId())
                                .texte(commentaire.getTexte())
                                // Si l'auteur est null, affiche "Auteur inconnu".
                                .nomAuteur(commentaire.getAuteur() != null ? commentaire.getAuteur().getUsername() : "Auteur inconnu")
                                .build();
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(commentaireDTOList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * @author Benoit
     *
     * Supprime un commentaire selon son ID.
     * Accessible uniquement aux administrateurs.
     * @param id ID du commentaire à supprimer
     * @return Message de succès ou d'erreur
     */
    @DeleteMapping("/commentaires/supprimer/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> deleteCommentaire(@PathVariable Long id) {
        try {
            // Appel du service pour supprimer le commentaire.
            commentairesService.supprimerCommentaire(id);
            return ResponseEntity.ok("Commentaire supprimé avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur lors de la suppression du commentaire.");
        }
    }

}

package org.example.projet_final.services;

import org.apache.tika.Tika;
import org.example.projet_final.config.FileStorageProperties;
import org.example.projet_final.models.*;
import org.example.projet_final.models.Requests.CommentaireRequest;
import org.example.projet_final.models.dto.FichierDTO;
import org.example.projet_final.models.dto.PhotoProfilDTO;
import org.example.projet_final.repositories.FileMetaDataRepository;
import org.example.projet_final.repositories.FichierProprietaireRepository;
import org.example.projet_final.repositories.UtilisateurRepository;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FileService {
    private final FileStorageProperties properties;
    private final FileMetaDataRepository repository;
    private final FileStorageService service;
    private final UtilisateurRepository utilisateurRepository;
    private final CommentairesService commentairesService;
    private final FichierProprietaireRepository fichierProprietaireRepository;

    public FileService(FileStorageProperties properties,
                       FileMetaDataRepository repository,
                       FileStorageService service,
                       CommentairesService commentairesService,
                       FichierProprietaireRepository fichierProprietaireRepository,
                       UtilisateurRepository utilisateurRepository)
    {
        this.properties = properties;
        this.repository = repository;
        this.service = service;
        this.commentairesService = commentairesService;
        this.fichierProprietaireRepository = fichierProprietaireRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * @author John / Benoit
     *
     * Téléverse un fichier pour un utilisateur en le stockant sur le serveur
     * @param file Le fichier téléversé par l'utilisateur
     * @param utilisateur L'utilisateur propriétaire du fichier
     * @return Les métadonnées du fichier enregistré
     * @throws IOException En cas d'erreur lors de la lecture ou du stockage du fichier
     */
    @Transactional
    public FileMetaData uploadFile(MultipartFile file, Utilisateur utilisateur) throws IOException {
        validateFile(file); // Vérifie que le fichier est valide.

        // Stocke physiquement le fichier et récupère son chemin de stockage.
        String storagePath = service.storeFile(file.getInputStream(), file.getOriginalFilename(), utilisateur.getUsername());

        // Création des métadonnées liées au fichier.
        FileMetaData fileMetaData = FileMetaData.builder()
                .nomOriginal(file.getOriginalFilename())
                .nomStocke(storagePath)
                .mimeType(file.getContentType())
                .visible(true) // Le fichier est visible par défaut.
                .commentairesAutorises(true) // Les commentaires sont autorisés par défaut.
                .build();

        // Création de l'objet liant le fichier à son propriétaire.
        FichierProprietaire proprietaire = FichierProprietaire.builder()
                .fileData(fileMetaData)
                .proprietaire(utilisateur) // Définit l'utilisateur comme propriétaire.
                .build();

        // Sauvegarde du fichier et de sa relation avec l'utilisateur dans la base de données.
        fichierProprietaireRepository.save(proprietaire);

        return fileMetaData;
    }

    /**
     * author Benoit
     *
     * Téléverse une image de profil pour un utilisateur.
     *
     * @param file Le fichier image envoyé (image de profil)
     * @param username Le nom d'utilisateur, utilisé pour organiser le stockage
     * @return Les métadonnées de l’image sauvegardée
     * @throws IOException Si un problème survient pendant la sauvegarde ou la lecture du fichier
     */
    public FileMetaData uploadImageProfil(MultipartFile file, String username) throws IOException {
        validateImage(file); // Vérifie que le fichier est valide.

        // Récupère le nom original du fichier tel que fourni par l'utilisateur.
        String originalFilename = file.getOriginalFilename();

        // Génère un nom unique afin d'éviter les conflits de noms de fichiers.
        String uniqueName = UUID.randomUUID() + "_" + originalFilename;

        String storagePath;
        try (InputStream inputStream = file.getInputStream()) {
            // Enregistre physiquement l'image sur le serveur.
            storagePath = service.storeFile(inputStream, uniqueName, username);
        }

        // Création des métadonnées de l'image de profil.
        FileMetaData fileMetaData = FileMetaData.builder()
                .nomOriginal(originalFilename)
                .nomStocke(storagePath)
                .mimeType(file.getContentType())
                .visible(true)  // Une image de profil est visible par défaut.
                .build();

        // Sauvegarde la photo de profil dans la base de données.
        return repository.save(fileMetaData);
    }

    /**
     * @author John / Benoit
     *
     * Valide un fichier envoyé avant son téléversement.
     *
     * @param file Le fichier envoyé par l’utilisateur
     * @throws IOException Si la lecture du fichier échoue
     */
    private void validateFile(MultipartFile file) throws IOException {
        // Utilisé pour analyser le contenu réel du fichier
        Tika tika = new Tika();

        // Vérifie que le fichier n'est pas vide
        if (file.isEmpty()) throw new IllegalArgumentException("Fichier vide");

        // Type MIME envoyé par le navigateur
        String mimeType = file.getContentType();
        String magicMimeType = tika.detect(file.getInputStream());

        // Compare les 2 types MIME
        if (!Objects.equals(magicMimeType, mimeType)) {
            throw new SecurityException("Type MIME invalide détecté");
        }

        // Vérifie que le type MIME fait partie des types autorisés dans la config.
        if (!properties.allowedMimeTypes().contains(mimeType)) {
            throw new IllegalArgumentException("Mime type non autorisé");
        }
    }

    /**
     * @author Benoit
     *
     * Valide l'image de profil avant son téléversement.
     * @param file L'image envoyée par l’utilisateur
     * @throws IOException Si la lecture du fichier échoue
     */
    private void validateImage(MultipartFile file) throws IOException {
        // Vérifie que le fichier n’est pas vide
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        Tika tika = new Tika();
        String detected = tika.detect(file.getInputStream());

        // Définition des types d'images autorisés.
        Set<String> allowedImageTypes = Set.of(
                "image/jpeg",
                "image/png",
                "image/webp",
                "image/gif"
        );

        // Vérifie que le type MIME détecté est bien une image autorisée.
        if (!allowedImageTypes.contains(detected)) {
            throw new IllegalArgumentException("Seules les images sont autorisées. Type détecté : " + detected);
        }
    }

    /**
     * @author John
     *
     *  Récupère les métadonnées d'un fichier à partir de son ID.
     *  Vérifie également que l'utilisateur actuel a les droits nécessaires pour voir ce fichier.
     *  Si l'utilisateur n'a pas les droits ou si le fichier n'est pas trouvé, une exception est levée.
     *
     * @param fileId L'ID du fichier pour lequel récupérer les métadonnées.
     * @return Les métadonnées du fichier.
     * @throws IllegalArgumentException Si le fichier avec l'ID spécifié n'existe pas.
     * @throws SecurityException Si l'utilisateur actuel n'a pas les droits d'accès au fichier.
     */
    public FileMetaData getFileMetadata(Long fileId) {
        FileMetaData file = repository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("Fichier introuvable"));

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // Vérifie si l'utilisateur actuel a les droits d'accès
        if (!canSee(file, currentUserEmail))
            throw new SecurityException("Accès refusé");

        return file;
    }

    /**
     * @author John
     *
     *  Récupère un fichier à partir de son ID en utilisant ses métadonnées.
     *  Cette méthode appelle {@link #getFileMetadata(Long)} pour vérifier l'existence et les droits d'accès au fichier avant de le récupérer.
     *
     * @param fileId L'ID du fichier à récupérer.
     * @return La ressource représentant le fichier.
     * @throws IOException En cas d'erreur lors de la lecture du fichier.
     * @throws IllegalArgumentException Si le fichier n'existe pas.
     * @throws SecurityException Si l'utilisateur actuel n'a pas les droits d'accès au fichier.
     */
    public Resource getFile(Long fileId) throws IOException {
        FileMetaData fileMetaData = getFileMetadata(fileId);
        return service.getFileResource(fileMetaData.getNomStocke());
    }




    /**
     * @author John / Benoit
     *
     * Supprime un fichier et ses références dans la base de données.
     *
     * @param fileId L'identifiant du fichier à supprimer
     */
    @Transactional
    public void deleteFile(Long fileId) {
        FileMetaData file = repository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        // Supprime le fichier physique sur le disque.
        try {
            service.supprimerFichierPhysique(file.getNomStocke());
        } catch (IOException e) {
            throw new RuntimeException("Impossible de supprimer le fichier physique", e);
        }

        // Récupère l'entité FichierProprietaire associée.
        FichierProprietaire fp = fichierProprietaireRepository.findByFileData(file)
                .orElseThrow(() -> new RuntimeException("FichierProprietaire introuvable"));

        fichierProprietaireRepository.delete(fp);  // Cascade supprimera FileMetaData.
    }

    /**
     * @author Benoit
     *
     * Supprime la photo de profil et ses références dans la base de données.
     *
     * @param fileMetaData
     */
    @Transactional
    public void deleteProfilePhoto(FileMetaData fileMetaData) {
        // Vérifie si le fichier existe.
        if (fileMetaData == null) return;

        // Supprime le fichier physique.
        try {
            service.supprimerFichierPhysique(fileMetaData.getNomStocke());
        } catch (IOException e) {
            throw new RuntimeException("Impossible de supprimer le fichier physique", e);
        }

        // Supprime l'entité FichierProprietaire si elle existe.
        fichierProprietaireRepository.findByFileData(fileMetaData)
                .ifPresent(fichierProprietaireRepository::delete);

        // Supprime l'entité FileMetaData.
        repository.delete(fileMetaData);
    }

    /**
     * @author John
     *
     *  Permet à un utilisateur d'ajouter un commentaire à un fichier, si les commentaires sont autorisés pour ce fichier.
     *  Le commentaire est ajouté à la liste des commentaires du fichier et celui-ci est enregistré.
     *
     * @param request L'objet contenant le texte du commentaire et l'ID du fichier.
     * @param auteur L'utilisateur qui ajoute le commentaire.
     * @return Le DTO du fichier avec le nouveau commentaire ajouté.
     * @throws RuntimeException Si le fichier n'est pas trouvé ou si les commentaires ne sont pas autorisés.
     */
    @Transactional
    public FichierDTO commenter(CommentaireRequest request, Utilisateur auteur) {
        FileMetaData file = repository.findById(request.getFichierId())
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        if (!file.isCommentairesAutorises()) return mapToDTO(file);

        Commentaire commentaire = new Commentaire();
        commentaire.setTexte(request.getTexte());
        commentaire.setSignaler(false);
        commentaire.setFichier(file);
        commentaire.setAuteur(auteur);

        file.getCommentaires().add(commentaire);
        repository.save(file);

        return mapToDTO(file);
    }


    /**
     * @author John
     *
     *  Ajoute un contributeur à un fichier, si l'utilisateur est le propriétaire du fichier.
     *  Vérifie que le contributeur n'est pas déjà dans la liste des contributeurs avant de l'ajouter.
     *
     * @param fichierId L'ID du fichier auquel ajouter un contributeur.
     * @param ownerEmail L'email du propriétaire du fichier.
     * @param contributor L'utilisateur à ajouter en tant que contributeur.
     * @throws RuntimeException Si l'utilisateur est déjà contributeur ou si l'accès est refusé.
     */
    @Transactional
    public void addContributor(Long fichierId, String ownerEmail, Utilisateur contributor) {
        FichierProprietaire fp = getProprioFile(fichierId, ownerEmail);

        if (fp.getContributeurs().contains(contributor))
            throw new RuntimeException("Utilisateur déjà contributeur");

        fp.getContributeurs().add(contributor);
        fichierProprietaireRepository.save(fp);
    }

    /**
     * @author John
     *
     *  Supprime un contributeur d'un fichier, si l'utilisateur est le propriétaire du fichier.
     *  Vérifie que le contributeur est bien dans la liste avant de le supprimer.
     *
     * @param fichierId L'ID du fichier duquel retirer un contributeur.
     * @param ownerEmail L'email du propriétaire du fichier.
     * @param contributor L'utilisateur à retirer en tant que contributeur.
     * @throws RuntimeException Si l'utilisateur n'est pas un contributeur ou si l'accès est refusé.
     */
    @Transactional
    public void removeContributor(Long fichierId, String ownerEmail, Utilisateur contributor) {
        FichierProprietaire fp = getProprioFile(fichierId, ownerEmail);

        if (!fp.getContributeurs().contains(contributor))
            throw new RuntimeException("Utilisateur non contributeur");

        fp.getContributeurs().remove(contributor);
        fichierProprietaireRepository.save(fp);
    }




    /**
     * @author John
     *
     *  Récupère la liste des contributeurs d'un fichier donné.
     *
     * @param fichierId L'ID du fichier pour lequel récupérer la liste des contributeurs.
     * @param ownerEmail L'email du propriétaire du fichier.
     * @return Un ensemble d'utilisateurs représentant les contributeurs du fichier.
     */
    public Set<Utilisateur> listContributors(Long fichierId, String ownerEmail) {
        return getProprioFile(fichierId, ownerEmail).getContributeurs();
    }



    /**
     * @author John
     *
     *  Change la visibilité d'un fichier (visible/non visible), si l'utilisateur est le propriétaire.
     *  Sauvegarde le changement dans la base de données.
     *
     * @param fichierId L'ID du fichier dont la visibilité doit être changée.
     * @param ownerEmail L'email du propriétaire du fichier.
     * @return La nouvelle visibilité du fichier (true si visible, false si non visible).
     */
    @Transactional
    public boolean changerVisibilite(Long fichierId, String ownerEmail) {
        FichierProprietaire fp = getProprioFile(fichierId, ownerEmail);

        FileMetaData file = fp.getFileData();
        file.setVisible(!file.isVisible());
        repository.save(file);

        return file.isVisible();
    }


    /**
     * @author John
     *
     *  Permet de basculer l'autorisation des commentaires pour un fichier (activer/désactiver).
     *  L'utilisateur doit être le propriétaire du fichier pour effectuer cette action.
     *
     * @param id L'ID du fichier pour lequel activer ou désactiver les commentaires.
     * @param email L'email de l'utilisateur qui effectue l'action (doit être le propriétaire du fichier).
     * @return Le nouvel état des commentaires (true si activés, false si désactivés).
     * @throws RuntimeException Si le fichier est introuvable.
     * @throws SecurityException Si l'utilisateur n'a pas les droits pour modifier l'état des commentaires.
     */
    public boolean toggleCommentaires(Long id, String email) {
        FileMetaData file = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        if (!file.getProprietaire().getProprietaire().getEmail().equals(email)) {
            throw new SecurityException("Accès refusé");
        }

        boolean newState = !file.isCommentairesAutorises();
        file.setCommentairesAutorises(newState);
        repository.save(file);

        return newState;
    }


    /**
     * @author John
     *
     *  Convertit les métadonnées d'un fichier en un DTO contenant des informations publiques sur le fichier.
     *  Vérifie si l'utilisateur a l'accès au fichier avant de renvoyer le DTO.
     *
     * @param file Les métadonnées du fichier à convertir en DTO.
     * @return Un DTO contenant les informations publiques du fichier.
     * @throws SecurityException Si l'utilisateur n'a pas l'accès au fichier.
     */
    public FichierDTO mapToDTO(FileMetaData file) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!canSee(file, currentUserEmail))
            throw new SecurityException("Accès refusé");

        FichierProprietaire fp = fichierProprietaireRepository.findByFileData(file).orElse(null);
        if (fp == null || fp.getProprietaire() == null)
            return null;

        return new FichierDTO(
                file.getId(),
                file.getNomOriginal(),
                file.getNomStocke(),
                file.getMimeType(),
                file.isVisible(),
                file.isCommentairesAutorises(),
                fp.getProprietaire().getUsername(),
                //Map les commentaires
                file.getCommentaires().stream()
                        .map(commentairesService::maptoDTO)
                        .collect(Collectors.toList())
        );
    }


    /**
     * @author John
     *
     *  Convertit les métadonnées d'un fichier photo de profil en un DTO.
     *
     * @param file Les métadonnées du fichier de photo de profil à convertir en DTO.
     * @return Un DTO contenant les informations sur la photo de profil.
     */
    public PhotoProfilDTO mapToPhotoProfilDto(FileMetaData file) {
        return new PhotoProfilDTO(
                file.getNomOriginal(),
                file.getNomStocke(),
                file.getMimeType()
        );
    }
    /**
     * @author John
     *
     *  Vérifie si un utilisateur a l'autorisation de voir un fichier en fonction de sa visibilité et de ses droits d'accès.
     *
     * @param file Le fichier à vérifier.
     * @param userEmail L'email de l'utilisateur qui tente d'accéder au fichier.
     * @return true si l'utilisateur a l'accès au fichier, false sinon.
     */
    private boolean canSee(FileMetaData file, String userEmail) {
        if (file.isVisible()) return true;

        FichierProprietaire fp = fichierProprietaireRepository.findByFileData(file).orElse(null);
        if (fp == null) return false;

        //si il est proprio
        if (fp.getProprietaire() != null && fp.getProprietaire().getEmail().equalsIgnoreCase(userEmail))
            return true;

        //ou si il fait partie des contributeurs
        return fp.getContributeurs().stream()
                .anyMatch(c -> c.getEmail().equalsIgnoreCase(userEmail));
    }

    /**
     * @author John
     *
     *  Récupère le propriétaire d'un fichier en vérifiant les droits d'accès de l'utilisateur.
     *  Cette méthode permet de récupérer le propriétaire d'un fichier et de vérifier si l'utilisateur est bien le propriétaire.
     *
     * @param fichierId L'ID du fichier pour lequel récupérer le propriétaire.
     * @param ownerEmail L'email du propriétaire du fichier.
     * @return Le propriétaire du fichier.
     * @throws RuntimeException Si le fichier n'existe pas ou si l'accès est refusé.
     */
    public FichierProprietaire getProprioFile(Long fichierId, String ownerEmail) {
        FileMetaData file = repository.findById(fichierId)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        FichierProprietaire fp = fichierProprietaireRepository.findByFileData(file)
                .orElseThrow(() -> new RuntimeException("FichierProprietaire introuvable"));

        if (!fp.getProprietaire().getEmail().equalsIgnoreCase(ownerEmail))
            throw new RuntimeException("Accès refusé : seul le propriétaire peut agir");

        return fp;
    }






    /**
     * @author John
     *
     *  Récupère tous les fichiers accessibles pour l'utilisateur actuel et les convertit en DTO.
     *  Vérifie les droits d'accès pour chaque fichier avant de le retourner.
     *
     * @return Une liste de DTOs représentant tous les fichiers accessibles à l'utilisateur.
     */
    public List<FichierDTO> getAllDto() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        return repository.findAll().stream()
                .filter(file -> canSee(file, currentUserEmail))
                .map(this::mapToDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * @author John
     *
     *  Récupère tous les fichiers associés à un auteur (utilisateur) donné et les convertit en DTO.
     *
     * @param utilisateur L'utilisateur pour lequel récupérer les fichiers associés.
     * @return Une liste de DTOs représentant les fichiers de l'utilisateur.
     */
    public List<FichierDTO> getFilesByAuteur(Utilisateur utilisateur) {
        return fichierProprietaireRepository.findByProprietaire(utilisateur).stream()
                .map(fp -> mapToDTO(fp.getFileData()))
                .collect(Collectors.toList());
    }




    /**
     * @author Benoit
     *
     *  Récupère l'image de profil par défaut si aucune image est envoyé lors de l'inscription.
     * @return Le FileMetaData de l'image de profil par défaut
     */
    public FileMetaData getDefaultProfileImage() {
        // Recherche de l'image par défaut dans la base de donnée.
        return repository.findFirstByNomStocke("profileImage_1.png")
                .orElseThrow(() -> new IllegalStateException("Photo de profil introuvable"));
    }

    /**
     * @author John
     *
     *  Récupère la liste des utilisateurs disponibles pour être ajoutés en tant que contributeurs à un fichier.
     *  La méthode vérifie d'abord que le fichier existe, puis exclut le propriétaire du fichier et les utilisateurs déjà contributeurs.
     *
     * @param idFichier L'ID du fichier pour lequel récupérer les contributeurs disponibles.
     * @param utilisateurCourant L'utilisateur actuel qui effectue la requête (utilisé pour vérifier si l'utilisateur est déjà contributeur ou propriétaire).
     * @return Un ensemble d'utilisateurs qui peuvent être ajoutés comme contributeurs au fichier.
     * @throws RuntimeException Si le fichier n'est pas trouvé.
     */
    public Set<Utilisateur> getAvailableContributors(Long idFichier, Utilisateur utilisateurCourant) {

        // Récupère les métadonnées du fichier.
        FileMetaData fileMetaData = getFileMetadata(idFichier);

        // Trouve le propriétaire du fichier.
        FichierProprietaire fichier = fichierProprietaireRepository.findByFileData(fileMetaData)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable"));

        // Récupère tous les utilisateurs.
        Set<Utilisateur> tous = new HashSet<>(utilisateurRepository.findAll());

        // Exclut le propriétaire du fichier des contributeurs disponibles.
        tous.remove(fichier.getProprietaire());

        // Exclut également tous les utilisateurs déjà contributeurs.
        tous.removeAll(fichier.getContributeurs());

        return tous;
    }




}

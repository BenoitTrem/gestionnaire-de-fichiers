package org.example.projet_final.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.example.projet_final.models.FichierProprietaire;
import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.Role;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.auth.RegisterRequest;
import org.example.projet_final.models.dto.UtilisateurDTO;
import org.example.projet_final.repositories.FichierProprietaireRepository;
import org.example.projet_final.repositories.FileMetaDataRepository;
import org.example.projet_final.repositories.RoleRepository;
import org.example.projet_final.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final FileMetaDataRepository fileMetaDataRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender javaMailSender;
    private final RoleRepository roleRepository;
    private final JWTUtilsService jwtUtilsService;
    private final FileService fileService;
    private final FichierProprietaireRepository fichierProprietaireRepository;

    @Value("${app.baseurl}")
    private String baseUrl;

    public UtilisateurService(UtilisateurRepository utilisateurRepository, FileMetaDataRepository fileMetaDataRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, JWTUtilsService jwtUtilsService, JavaMailSender javaMailSender, FileService fileService, FichierProprietaireRepository fichierProprietaireRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.fileMetaDataRepository = fileMetaDataRepository;
        this.jwtUtilsService = jwtUtilsService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.javaMailSender = javaMailSender;
        this.fileService = fileService;
        this.fichierProprietaireRepository = fichierProprietaireRepository;
    }

    public Utilisateur getByEmail(String email) {
        return utilisateurRepository.findByEmail(email).orElse(null);
    }

    public Utilisateur getByUsername(String username) {
        return utilisateurRepository.findByUsername(username).orElse(null);
    }

    public List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }

    /**
     * @author Benoit
     *
     * Ajoute un nouvel utilisateur dans la base de donnée et envoie un email de confirmation.
     *
     * @param request  Les informations de l'utilisateur à enregistrer
     * @param photoProfilFile La photo de profil associée à l'utilisateur
     * @return  L'utilisateur créé et sauvegardé
     * @throws MessagingException Si l'envoi de l'email échoue
     * @throws IOException  Si la manipulation de la photo échoue
     */
    public Utilisateur ajouter(RegisterRequest request, MultipartFile photoProfilFile) throws MessagingException, IOException {

        // Récupère le rôle par défaut "ROLE_USER".
        Role roleUser = roleRepository.findByName("ROLE_USER");
        if (roleUser == null) {
            throw new RuntimeException("Aucun rôle ROLE_USER trouvé");
        }

        // Construie l'objet Utilisateur avec les informations fournies.
        Utilisateur utilisateur = Utilisateur.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .estVirifie(false) // false par défault.
                .roles(Set.of(roleUser)) // Assigne le rôle à l'utilisateur.
                .build();

        FileMetaData photoProfil;

        // Vérifie si une photo de profil a été fournie.
        if (photoProfilFile != null && !photoProfilFile.isEmpty()) {
            // Upload de la photo de profil fournie.
            photoProfil = fileService.uploadImageProfil(photoProfilFile, utilisateur.getUsername());
        } else {
            // Sinon, assigne l'image de profil par défaut.
            photoProfil = fileService.getDefaultProfileImage();
        }
        // Assigne la photo de profil à l'utilisateur.
        utilisateur.setPhotoProfile(photoProfil);

        // Sauvegarde l'utilisateur dans la base de données.
        utilisateur = utilisateurRepository.save(utilisateur);

        // Envoie un email de confirmation à l'utilisateur.
        try {
            envoyerCourriel(utilisateur);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'envoi du mail : " + e.getMessage());
        }
        // Retourne l'utilisateur créé.
        return utilisateur;
    }

    /**
     * @author Benoit
     *
     * Supprime un utilisateur et tous ses fichiers associés.
     *
     * @param id L'identifiant de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false si l'utilisateur n'existe pas
     */
    @Transactional
    public boolean supprimerUtilisateur(Integer id) {
        // Récupère l'utilisateur par son ID.
        Utilisateur utilisateur = utilisateurRepository.findById(id).orElse(null);
        if (utilisateur == null) return false; // Retourner false si l'utilisateur n'existe pas.

        // Supprime la photo de profil si elle existe.
        FileMetaData profilePhoto = utilisateur.getPhotoProfile();
        if (profilePhoto != null) {
            try {
                fileService.deleteProfilePhoto(profilePhoto);
            } catch (Exception e) {
                System.out.println("Impossible de supprimer la photo de profil : " + e.getMessage());
            }
            utilisateur.setPhotoProfile(null);
        }

        // Supprime tous les fichiers possédés par l'utilisateur.
        List<FichierProprietaire> fichiers = fichierProprietaireRepository.findAllByProprietaire(utilisateur);
        for (FichierProprietaire fp : fichiers) {
            try {
                fileService.deleteFile(fp.getFileData().getId()); // Supprime le fichier physique et ses métadonnées.
                fichierProprietaireRepository.delete(fp); // Supprime la relation propriétaire.
            } catch (Exception e) {
                System.out.println("Impossible de supprimer le fichier : " + e.getMessage());
            }
        }
        // Supprime l'utilisateur de la base de données.
        utilisateurRepository.delete(utilisateur);
        return true;
    }

    /**
     * @author Benoit
     *
     *  Modifie les informations d'un utilisateur
     *
     * @param id  Modifie les informations d'un utilisateur
     * @param email Le nouvel email
     * @param password Le nouvel email
     * @param photoProfile  La nouvelle photo de profil
     * @param utilisateur L'utilisateur connecté
     * @return L'utilisateur mis à jour
     * @throws IOException  En cas d'erreur lors du traitement de la photo de profil
     */
    @Transactional
    public Utilisateur modifierUtilisateur(Integer id, String email, String password,
                                           MultipartFile photoProfile, Utilisateur utilisateur) throws IOException {

        // Vérifie que l'utilisateur connecté correspond à l'utilisateur à modifier.
        if (!utilisateur.getId().equals(id)) {
            throw new IllegalArgumentException("Vous ne pouvez pas modifier un compte qui ne vous appartient pas.");
        }

        // Metà jour l'email si fourni et si il est différent de l'actuel.
        if (email != null && !email.equals(utilisateur.getEmail())) {
            utilisateur.setEmail(email);
        }

        // Met à jour le mot de passe si fourni et non vide.
        if (password != null && !password.isEmpty()) {
            utilisateur.setPassword(passwordEncoder.encode(password));
        }

        // Met à jour la photo de profil si un fichier est fourni.
        if (photoProfile != null && !photoProfile.isEmpty()) {
            FileMetaData oldPhoto = utilisateur.getPhotoProfile();

            // Supprimer l'ancienne photo de profil si elle existe.
            if (oldPhoto != null) {
                fileService.deleteProfilePhoto(oldPhoto);
            }

            // Définie la nouvelle photo de profil.
            FileMetaData newPhoto = fileService.uploadImageProfil(photoProfile, utilisateur.getUsername());
            utilisateur.setPhotoProfile(newPhoto);
        }
        // Sauvegarder les modifications dans la base de données.
        return utilisateurRepository.save(utilisateur);
    }

    /**
     * @author Benoit
     *
     * Modifie le rôle d'un utilisateur donné.
     *
     * @param userId  L'identifiant de l'utilisateur dont on veut changer le rôle
     * @param nouveauRole Le nom du nouveau rôle à attribuer
     * @return Message indiquant le résultat
     */
    public String modifierRoleUtilisateur(Integer userId, String nouveauRole) {
        // Récupère l'utilisateur par son ID
        Utilisateur utilisateur = utilisateurRepository.findById(userId).orElse(null);
        if (utilisateur == null) {
            return "Utilisateur non trouvé.";
        }

        // Récupère le rôle à attribuer.
        Role role = roleRepository.findByName(nouveauRole);
        if (role == null) {
            return "Rôle invalide.";
        }

        // Supprime tous les rôles actuels et ajoute le nouveau rôle.
        utilisateur.getRoles().clear();
        utilisateur.getRoles().add(role);

        // Sauvegarde les modifications dans la base de données.
        utilisateurRepository.save(utilisateur);

        return "Role modifié";
    }

    @Transactional
    public void sauvegarder(Utilisateur utilisateur) {
        utilisateurRepository.save(utilisateur);
    }

    /**
     * @author Bruno
     *
     *  Envoie un courriel de confirmation à un utilisateur.
     *
     * @param utilisateur L'utilisateur à qui envoyer le courriel de confirmation.
     * @throws MessagingException En cas d'erreur lors de la création ou de l'envoi du message.
     */
    public void envoyerCourriel(Utilisateur utilisateur) throws MessagingException {

        String email = utilisateur.getEmail();
        //pour deboguer
        System.out.println(">>> Envoi de courriel à : " + email);

        String token = jwtUtilsService.genererToken(email);

        String lienConfirmation = baseUrl + "/valider?token=" + token;

        //affichage
        String html = """
    <html>
      <body>
        <h3>Confirmez votre adresse e-mail</h3>
        <p>Merci de confirmer votre adresse e-mail en cliquant sur le lien ci-dessous :</p>
        <a href='%s'>Confirmer mon e-mail</a>
      </body>
    </html>
    """.formatted(lienConfirmation);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(email);
        helper.setFrom("no_reply@GestionFichiers.com");
        helper.setSubject("Confirmez votre courriel");
        helper.setText(html, true);

        //envoi
        javaMailSender.send(message);
    }

    /**
     * @author John
     *
     *  Envoie un courriel permettant à un utilisateur de réinitialiser son mot de passe.
     *
     * @param email L'adresse courriel de l'utilisateur demandant la réinitialisation.
     * @throws MessagingException En cas d'erreur lors de la création ou de l'envoi du message.
     */
    public void envoyerCourrielPasswordoublier(String email) throws MessagingException {

        Optional<Utilisateur> u = utilisateurRepository.findByEmail(email);
        if (u.isEmpty()) throw new RuntimeException("Email inexistant");

        String token = jwtUtilsService.genererToken(email);

        String lien = baseUrl + "/motdepasseRT?token=" + token;

        String html = """
    <html>
      <body>
        <h3>Réinitialisation du mot de passe</h3>
        <p>Cliquez sur ce lien pour définir un nouveau mot de passe :</p>
        <a href='%s'>Réinitialiser le mot de passe</a>
      </body>
    </html>
    """.formatted(lien);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(email);
        helper.setFrom("no_reply@GestionFichiers.com");
        helper.setSubject("Mot de passe oublié");
        helper.setText(html, true);

        javaMailSender.send(message);
    }

    public Optional<Utilisateur> finByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    /**
     * @author John
     *
     *  Convertit un objet Utilisateur en UtilisateurDTO.
     *
     * @param utilisateur L'utilisateur à convertir.
     * @return Un UtilisateurDTO contenant les informations publiques de l'utilisateur.
     */
    public UtilisateurDTO mapToDTO(Utilisateur utilisateur) {

        //Prendre que les noms des roles de l'user
        Set<String> roleNames = utilisateur.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        //Créer le dto
        return new UtilisateurDTO(
                utilisateur.getId(),
                utilisateur.getUsername(),
                utilisateur.getEmail(),
                roleNames,
                //Map la photo de profil
                fileService.mapToPhotoProfilDto(utilisateur.getPhotoProfile())
        );
    }

    /**
     * @author John/Benoit
     *
     *  Récupère l'utilisateur actuellement authentifié.
     *
     * @return L'utilisateur connecté.
     * @throws RuntimeException Si aucun utilisateur authentifié n'est trouvé.
     */
    public Utilisateur getUtilisateur() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new RuntimeException("Utilisateur introuvable");
        }

        return utilisateurRepository.findByEmail(auth.getName())
                .orElseThrow(() ->
                        new RuntimeException("Utilisateur introuvable (email: " + auth.getName() + ")"));
    }

}

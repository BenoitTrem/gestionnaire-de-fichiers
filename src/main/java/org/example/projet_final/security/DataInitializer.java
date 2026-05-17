package org.example.projet_final.security;

import org.example.projet_final.models.*;
import org.example.projet_final.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

/**
 * @author Benoit / John
 */
@Configuration
public class DataInitializer {
    /**
     * Initialise la base de données au démarrage.
     * Ajoute les rôles, utilisateurs, fichiers et commentaires si la BD est vide.
     */
    @Bean
    public CommandLineRunner seedData(
            RoleRepository roleRepository,
            UtilisateurRepository utilisateurRepository,
            FileMetaDataRepository fileMetaDataRepository,
            FichierProprietaireRepository fichierProprietaireRepository,
            CommentaireRepository commentaireRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {

            // Création du rôle ADMIN
            Role adminRole = roleRepository.findByName("ROLE_ADMIN");
            if (adminRole == null) {
                adminRole = roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
            }

            // Création du rôle USER
            Role userRole = roleRepository.findByName("ROLE_USER");
            if (userRole == null) {
                userRole = roleRepository.save(Role.builder().name("ROLE_USER").build());
            }


            if (utilisateurRepository.count() == 0) {

                // Création des profils avec leurs rôles et image de profil.
                FileMetaData bobProfile = FileMetaData.builder()
                        .nomOriginal("profileImage_1.png")
                        .nomStocke("profileImage_1.png")
                        .mimeType("image/png")
                        .visible(true)
                        .commentairesAutorises(true)
                        .build();
                fileMetaDataRepository.save(bobProfile);

                Utilisateur bob = Utilisateur.builder()
                        .username("bob")
                        .email("bob@gmail.com")
                        .password(passwordEncoder.encode("password123"))
                        .roles(Set.of(userRole))
                        .estVirifie(true)
                        .photoProfile(bobProfile)
                        .build();
                utilisateurRepository.save(bob);

                FileMetaData annaProfile = FileMetaData.builder()
                        .nomOriginal("profileImage_1.png")
                        .nomStocke("profileImage_1.png")
                        .mimeType("image/png")
                        .visible(true)
                        .commentairesAutorises(true)
                        .build();
                fileMetaDataRepository.save(annaProfile);

                Utilisateur anna = Utilisateur.builder()
                        .username("anna")
                        .email("anna@gmail.com")
                        .password(passwordEncoder.encode("password123"))
                        .roles(Set.of(userRole))
                        .estVirifie(true)
                        .photoProfile(annaProfile)
                        .build();
                utilisateurRepository.save(anna);

                FileMetaData adminProfile = FileMetaData.builder()
                        .nomOriginal("profileImage_2.png")
                        .nomStocke("profileImage_2.png")
                        .mimeType("image/png")
                        .visible(true)
                        .commentairesAutorises(true)
                        .build();
                fileMetaDataRepository.save(adminProfile);

                Utilisateur admin = Utilisateur.builder()
                        .username("Admin")
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("password123"))
                        .roles(Set.of(adminRole))
                        .estVirifie(true)
                        .photoProfile(adminProfile)
                        .build();
                utilisateurRepository.save(admin);

                // Fichiers créés avec leurs contributeurs.
                FichierProprietaire fp1 = FichierProprietaire.builder()
                        .proprietaire(bob)
                        .fileData(FileMetaData.builder()
                                .nomOriginal("document_bob.pdf")
                                .nomStocke("document_bob.pdf")
                                .mimeType("application/pdf")
                                .visible(true)
                                .commentairesAutorises(true)
                                .build())
                        .build();
                fichierProprietaireRepository.save(fp1);
                fp1.getContributeurs().add(anna);
                fp1.getContributeurs().add(admin);
                fichierProprietaireRepository.save(fp1);

                FichierProprietaire fp2 = FichierProprietaire.builder()
                        .proprietaire(anna)
                        .fileData(FileMetaData.builder()
                                .nomOriginal("photo_de_vacance.jpg")
                                .nomStocke("photo_de_vacance.jpg")
                                .mimeType("image/jpeg")
                                .visible(true)
                                .commentairesAutorises(true)
                                .build())
                        .build();
                fichierProprietaireRepository.save(fp2);
                fp2.getContributeurs().add(bob);
                fichierProprietaireRepository.save(fp2);

                FichierProprietaire fp3 = FichierProprietaire.builder()
                        .proprietaire(admin)
                        .fileData(FileMetaData.builder()
                                .nomOriginal("note_admin.txt")
                                .nomStocke("note_admin.txt")
                                .mimeType("text/plain")
                                .visible(true)
                                .commentairesAutorises(true)
                                .build())
                        .build();
                fichierProprietaireRepository.save(fp3);
                fp3.getContributeurs().add(bob);
                fp3.getContributeurs().add(anna);
                fichierProprietaireRepository.save(fp3);

                // Création des commentaires liés aux fichiés.
                commentaireRepository.save(Commentaire.builder()
                        .texte("Super document !")
                        .signaler(false)
                        .fichier(fp1.getFileData())
                        .auteur(bob)
                        .build());

                commentaireRepository.save(Commentaire.builder()
                        .texte("Pas mal, mais quelques corrections")
                        .signaler(true)
                        .fichier(fp1.getFileData())
                        .auteur(anna)
                        .build());

                commentaireRepository.save(Commentaire.builder()
                        .texte("Belle photo")
                        .signaler(false)
                        .fichier(fp2.getFileData())
                        .auteur(bob)
                        .build());

                commentaireRepository.save(Commentaire.builder()
                        .texte("Merci pour le fichier")
                        .signaler(false)
                        .fichier(fp3.getFileData())
                        .auteur(anna)
                        .build());
            }
        };
    }
}

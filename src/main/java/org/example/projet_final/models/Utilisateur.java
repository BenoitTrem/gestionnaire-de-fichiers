package org.example.projet_final.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.projet_final.models.auth.RefreshToken;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "utilisateur_id", nullable = false)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    private boolean estVirifie = false;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilisateurs_roles",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>(); // Liste des rôles associés.

    // Photo de profil de l'utilisateur.
    @ManyToOne
    @JoinColumn(name = "photo_profile_id")
    private FileMetaData photoProfile;


    // Les fichiers que cet utilisateur possède ou auxquels il a accès.
    @OneToMany(mappedBy = "proprietaire", cascade = CascadeType.ALL)
    private Set<FichierProprietaire> fichiersPossedes;

    // Les fichiers partagés avec cet utilisateur en tant que contributeur.
    @ManyToMany(mappedBy = "contributeurs")
    private Set<FichierProprietaire> fichiersPartages;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new HashSet<>(); // Son refresh token.


}



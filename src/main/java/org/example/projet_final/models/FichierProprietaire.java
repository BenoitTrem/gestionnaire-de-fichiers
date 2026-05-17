package org.example.projet_final.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "fichier_proprietaire")
public class FichierProprietaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Le propriétaire du fichier.
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur proprietaire;

    // Référence vers les données du fichier
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_data_id", nullable = false)
    private FileMetaData fileData;

    // Liste des utilisateurs ayant accès en contribution au fichier partagé.
    @ManyToMany
    @JoinTable(
            name = "fichier_contributeurs",
            joinColumns = @JoinColumn(name = "fichier_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    @Builder.Default
    private Set<Utilisateur> contributeurs = new HashSet<>();
}

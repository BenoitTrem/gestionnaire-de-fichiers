package org.example.projet_final.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "commentaires")
public class Commentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "commentaire_id")
    private Long id;

    private String texte;

    private boolean signaler;

    // Fichier associé au commentaire.
    @ManyToOne
    @JoinColumn(name = "fichier_id")
    private FileMetaData fichier;

    // Auteur du commentaire.
    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur auteur;
}

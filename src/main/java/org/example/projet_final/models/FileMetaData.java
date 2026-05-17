package org.example.projet_final.models;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "file_metadata")
public class FileMetaData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fichier_id", nullable = false)
    private Long id;

    private String nomOriginal;
    private String nomStocke;
    private String mimeType;

    private boolean visible;
    private boolean commentairesAutorises;

    // Liste des commentaires liés à ce fichier.
    @OneToMany(mappedBy = "fichier", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Commentaire> commentaires = new HashSet<>();

    // Informations sur le propriétaire du fichier.
    @OneToOne(mappedBy = "fileData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FichierProprietaire proprietaire;



}


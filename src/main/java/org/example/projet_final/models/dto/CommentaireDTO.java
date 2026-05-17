package org.example.projet_final.models.dto;


import lombok.*;


/**
 * @author John
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentaireDTO {
    private Long id;
    private String texte;
    private String nomAuteur;
}

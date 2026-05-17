package org.example.projet_final.models.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

/**
 * @author John
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FichierDTO {
    private Long idfichier;
    private String nom;
    private String nomStocke;
    private String mimeType;
    private boolean visible;
    private boolean commentairesAutorises;
    private String nomProprio;
    private List<CommentaireDTO> commentaires;


}

package org.example.projet_final.models.Requests;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * @author John
 */
@Data
@AllArgsConstructor
public class CommentaireRequest {
    private Long FichierId;
    private String Texte;
}

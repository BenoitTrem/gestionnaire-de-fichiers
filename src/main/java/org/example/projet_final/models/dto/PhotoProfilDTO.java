package org.example.projet_final.models.dto;

import lombok.*;

/**
 * @author Benoit
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoProfilDTO {
    private String nomOriginal;
    private String nomStocke;
    private String mimeType;
}

package org.example.projet_final.models.dto;

import lombok.*;

import java.util.Set;

/**
 * @author Benoit
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO {
    private Integer id;
    private String username;
    private String email;
    private Set<String> roles;
    private PhotoProfilDTO photoProfile;
}


package org.example.projet_final.models.auth;

import lombok.*;
import java.util.Set;


/**
 * @author Benoit
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private String email;
    private Set<String> roles;
    private String message;
    private Integer userId;

    public AuthResponse(String accessToken, String email, Set<String> roles, String message, Integer userId) {
        this.accessToken = accessToken;
        this.tokenType = "Bearer";
        this.email = email;
        this.roles = roles;
        this.message = message;
        this.userId = userId;
    }
}

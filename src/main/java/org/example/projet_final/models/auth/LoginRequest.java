package org.example.projet_final.models.auth;

import lombok.*;

/**
 * @author Benoit
 */
@Data
@AllArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
}

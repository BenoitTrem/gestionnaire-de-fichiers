package org.example.projet_final.models.auth;

import lombok.Data;

/**
 * @author Benoit
 */
@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String confirmerPassword;

    public RegisterRequest(String username, String email, String password, String confirmerPassword) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.confirmerPassword = confirmerPassword;
    }
}


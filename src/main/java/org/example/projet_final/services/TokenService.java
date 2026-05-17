package org.example.projet_final.services;

import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.auth.RefreshToken;
import org.example.projet_final.repositories.RefreshTokenRepository;
import org.example.projet_final.repositories.UtilisateurRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TokenService {
    // Utiliser l'encodeur créé dans SecurityConfig.java
    private final JwtEncoder jwtEncoder;
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JwtEncoder jwtEncoder, UtilisateurRepository utilisateurRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.utilisateurRepository = utilisateurRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }


    /**
     * @author Bruno
     *
     * Donne en paramètre l'objet authentication pour avoir accès aux détails de l'utilisateur
     *
     * @param authentication L'objet Authentication fourni par Spring Security
     * @return Le token JWT sous forme de chaîne de caractères
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        // Récupère les rôles.
        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        // Construie le jeton.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                //.expiresAt(now.plus(10, ChronoUnit.SECONDS))
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))

                .subject(authentication.getName())
                // On ajoute un "claim" personnalisé contenant les rôles. On pourra s'en servir pour les autorisations.
                // La clé doit corresponde à la clé du convertisseur d'authentification.

                .claim("roles", roles)
                .build();
        // Retourner le jeton créé sous forme de String.
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * @author Bruno
     *
     * Crée un refresh token pour un utilisateur donné.
     *
     * @param userId  L'identifiant de l'utilisateur
     * @return  Le RefreshToken créé et sauvegardé
     */
    public RefreshToken creerRefreshToken(Integer userId) {
        // Récupère l'utilisateur depuis le repository.
        Utilisateur user = utilisateurRepository.findById(userId).get();

        RefreshToken refreshToken = RefreshToken.builder()
                .utilisateur(user)
                .token(UUID.randomUUID().toString())
                //.expiryDate(Instant.now().plus(15, ChronoUnit.SECONDS))
                .expiryDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    /**
     * @author Benoit
     *
     * Valide un refresh token
     *
     * @param token Le token à valider
     * @return Le RefreshToken correspondant si valide
     */
    public RefreshToken validerRefreshToken(String token) {
        System.out.println("Token validé: " + token);

        // Recherche le token dans la base de donnée.
        Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByToken(token);
        if (optionalRefreshToken.isEmpty()) {
            System.out.println("Token introuvable");
            throw new RuntimeException("Token introuvable");
        }

        RefreshToken refreshToken = optionalRefreshToken.get();
        System.out.println("Token trouvé: " + refreshToken);

        // Vérifie si le token est expiré.
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken); // Supprime le token expiré.
            System.out.println("Jeton de refraîchissement expiré");
            throw new RuntimeException("Jeton de refraîchissement expiré");
        }
        // Token validé.
        System.out.println("Validé");
        return refreshToken;
    }

    /**
     * @author Benoit
     *
     * Supprime un refresh token de la base de données.
     *
     * @param refreshToken
     */
    public void deleteToken(RefreshToken refreshToken) {
        // Supprime le token fourni.
        refreshTokenRepository.delete(refreshToken);
    }

}


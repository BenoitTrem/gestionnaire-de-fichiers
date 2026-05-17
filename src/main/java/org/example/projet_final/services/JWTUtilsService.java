package org.example.projet_final.services;

import io.jsonwebtoken.*;
import org.example.projet_final.security.RsaKeyProperties;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;


@Service
public class JWTUtilsService {

    private final RsaKeyProperties rsaKeyProperties;

    public JWTUtilsService(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }


    /**
     * @author Bruno
     *
     *  Génère un jeton JWT pour un utilisateur basé sur son adresse email.
     *  Ce jeton sera signé avec une clé RSA privée et expirera après une heure.
     *
     * @param email L'adresse e-mail de l'utilisateur pour lequel générer le jeton.
     * @return Le jeton JWT généré.
     */
    public String genererToken(String email) {
        Instant now = Instant.now();

        RSAPrivateKey privateKey = rsaKeyProperties.rsaPrivateKey();

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }



    /**
     * @author Bruno
     *
     *  Valide un jeton JWT et extrait l'adresse e-mail de l'utilisateur à partir du sujet du jeton.
     *  Si le jeton est expiré ou invalide, une exception est levée.
     *
     * @param token Le jeton JWT à valider.
     * @return L'adresse e-mail de l'utilisateur extraite du jeton.
     * @throws RuntimeException Si le jeton est expiré, invalide ou si la signature est incorrecte.
     */
    public String validerJetonEtExtraireCourriel(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(rsaKeyProperties.rsaPublicKey())
                    .build();

            Jws<Claims> claims = parser.parseSignedClaims(token);

            return claims.getPayload().getSubject();

        } catch (ExpiredJwtException e) {
            throw new RuntimeException("Jeton expiré", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new RuntimeException("Signature du jeton invalide", e);
        } catch (JwtException e) {
            throw new RuntimeException("Jeton invalide", e);
        }
    }

}

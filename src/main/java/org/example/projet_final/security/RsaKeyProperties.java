package org.example.projet_final.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Propriétés des clés RSA utilisées pour signer et vérifier les JWT.
 * Cette classe permet de charger automatiquement la clé publique et privée
 * définies dans application.properties.
 */
@ConfigurationProperties(prefix = "rsa")
public record RsaKeyProperties(RSAPublicKey rsaPublicKey, RSAPrivateKey rsaPrivateKey) {}




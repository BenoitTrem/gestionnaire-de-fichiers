package org.example.projet_final.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;


/**
 * @author Benoit
 *
 * Configuration des propriétés de stockage de fichiers.
 * Utilisé pour charger les valeurs depuis application.properties.
 * @param basePath
 * @param allowedMimeTypes
 */
@ConfigurationProperties(prefix = "app.file-storage")
public record FileStorageProperties(
        String basePath, // Chemin local ou S3
        Set<String> allowedMimeTypes // Types MIME acceptés
) {}


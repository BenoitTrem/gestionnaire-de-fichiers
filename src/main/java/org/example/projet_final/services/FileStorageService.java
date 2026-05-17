package org.example.projet_final.services;

import org.example.projet_final.config.FileStorageProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class FileStorageService {
    private final FileStorageProperties properties;
    private final Path rootPath;
    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
        this.rootPath = Paths.get(properties.basePath());
    }

    // Extrait de FileStorageService
    public String getBasePath() {
        return this.rootPath.toString();
    }

    /**
     * @author Benoit
     *
     * Crée un répertoire pour l'utilisateur basé sur son nom et la date du jour,
     * génère un nom de fichier unique, puis copie le contenu du fichier fourni.
     * Retourne le chemin relatif du fichier stocké.
     *
     * @param inputStream Le flux du fichier à stocker
     * @param originalFilename Le nom original du fichier
     * @param username Le nom de l'utilisateur propriétaire du fichier
     * @return Le chemin relatif du fichier stocké
     * @throws IOException Si une erreur survient lors de la création des répertoires ou de l'écriture du fichier
     */
    public String storeFile(InputStream inputStream, String originalFilename, String username) throws IOException {
        // Récupère la date actuelle.
        LocalDate today = LocalDate.now();

        // Crée le répertoire basé sur le nom de l'utilisateur et la date.
        Path dateDir = rootPath.resolve(username)
                .resolve(today.getYear() + "-" +
                        String.format("%02d", today.getMonthValue()) + "-" +
                        String.format("%02d", today.getDayOfMonth()));

        // Crée le répertoire si nécessaire.
        if (!Files.exists(dateDir)) {
            Files.createDirectories(dateDir);
        }

        // Récupère l'extension du fichier et génère un nom unique.
        String ext = getFileExtension(originalFilename);
        String filename = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        Path targetPath = dateDir.resolve(filename);

        // Copie le contenu du fichier dans le nouveau fichier sur le disque.
        try (OutputStream outputStream = Files.newOutputStream(targetPath)) {
            StreamUtils.copy(inputStream, outputStream);
        }

        // Retourne le chemin relatif pour pouvoir le stocker dans la base de données.
        return rootPath.relativize(targetPath).toString().replace("\\", "/");
    }


    /**
     * @author Bruno
     *
     *  Récupère une ressource de fichier à partir de son nom stocké.
     *  Vérifie que le fichier demandé se trouve bien dans le répertoire racine pour éviter toute tentative d'accès non autorisé.
     *  Si le fichier existe, il est retourné sous forme de ressource.
     *
     * @param storedName Le nom du fichier stocké sur le serveur.
     * @return La ressource représentant le fichier demandé.
     * @throws IOException En cas d'erreur lors de la lecture du fichier ou de son accès.
     * @throws SecurityException Si une tentative d'accès à un fichier en dehors du répertoire racine est détectée.
     * @throws FileNotFoundException Si le fichier n'existe pas.
     */
    public Resource getFileResource(String storedName) throws IOException {
        Path path = rootPath.resolve(storedName).normalize().toAbsolutePath();
        Path normalizedRootPath = rootPath.normalize().toAbsolutePath();

        // Vérification de la tentative d'accès en dehors du répertoire racine
        if (!path.startsWith(normalizedRootPath)) {
            throw new SecurityException("Access denied");
        }

        // Vérification de l'existence du fichier
        if (!Files.exists(path)) {
            throw new FileNotFoundException("File not found");
        }

        // Retourne la ressource du fichier
        return new UrlResource(path.toUri());
    }


    /**
     * @author Benoit
     * Récupère l'extension d'un fichier à partir de son nom.
     *
     * @param filename Le nom du fichier
     * @return L'extension du fichier
     */
    private String getFileExtension(String filename) {
        // Trouve la position du dernier point dans le nom de fichier, exemple: file. <- son extension.
        int lastDot = filename.lastIndexOf(".");

        // Si aucun point trouvé, retourne une chaîne vide ; sinon, retourne l'extension après le dernier point.
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }

    /**
     * @author Benoit
     *
     * Supprime un fichier physique du disque ainsi que les dossiers vides parents.
     *
     * @param storedName Le chemin relatif du fichier stocké à supprimer
     * @throws IOException Si une erreur survient lors de la suppression
     */
    public void supprimerFichierPhysique(String storedName) throws IOException {
        // Le chemin absolu du fichier à partir du chemin racine.
        Path filePath = rootPath.resolve(storedName).normalize().toAbsolutePath();

        // Vérifie si le fichier existe.
        if (Files.exists(filePath)) {
            Files.delete(filePath); // Supprime le fichier.

            // Vérifie si le dossier parent pour le supprimer s'il est vide.
            Path parent = filePath.getParent();
            Path userFolder = rootPath.resolve(filePath.getName(0)); // dossier de l'utilisateur.

            while (parent != null && !parent.equals(userFolder) && Files.isDirectory(parent)) {
                // Si le dossier est vide, supprime le dossier à la racine.
                if (Files.list(parent).findAny().isEmpty()) {
                    Files.delete(parent);
                } else {
                    break;
                }
                parent = parent.getParent();
            }
        }
    }
}

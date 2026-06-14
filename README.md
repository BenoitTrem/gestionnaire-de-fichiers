# JSP — Application Web Spring Boot

Application web full-stack avec backend Spring Boot (API REST + JWT) et frontend servi statiquement. Inclut la gestion des utilisateurs, l'authentification par JWT, le téléversement de fichiers et un système de commentaires.

---

## Fonctionnalités

- Création de compte et connexion avec token JWT
- Photo de profil et mise à jour des informations personnelles
- Suppression de compte
- Gestion des rôles (Admin)
- Validation du courriel et réinitialisation de mot de passe
- Téléversement et téléchargement de fichiers
- Affichage et ajout de commentaires
- Modération des commentaires rapportés

---

## Prérequis

- Java 17+
- Maven
- Un compte [Mailtrap](https://mailtrap.io) (pour les courriels de test)

---

## Configuration

**1. `application.properties`**

Ouvrez `src/main/resources/application.properties` et ajustez les valeurs suivantes selon votre environnement :

- `spring.datasource.url` — chemin vers votre fichier SQLite
- `spring.mail.username` et `spring.mail.password` — vos identifiants Mailtrap (disponibles dans **Email Testing → Inboxes → SMTP Settings**)
- `app.baseurl` — l'URL de base de votre serveur (par défaut `http://localhost:8080`)

**2. Clés RSA**

L'application utilise des clés RSA pour signer les tokens JWT. Générez une paire de clés et placez les fichiers dans :

```
src/main/resources/certs/
    private.pem
    public.pem
```

Les chemins sont déjà configurés dans `application.properties`. Ces fichiers sont dans le `.gitignore` et ne seront jamais committés.

**3. Base de données SQLite**

Créez un fichier vide nommé `database.sqlite` à la racine du projet avant de démarrer l'application.

---

## Démarrer l'application

**1. Démarrer le serveur**

```bash
./mvnw spring-boot:run
```

Ou depuis IntelliJ, ouvrez la classe principale et cliquez sur **Run**.

**2. Ouvrir le frontend**

Une fois le serveur démarré, ouvrez votre navigateur et accédez à :

```
http://localhost:8080/vues/index.html
```

---

## Comptes de test

La base de données est automatiquement peuplée au premier démarrage avec les comptes suivants :

| Nom d'utilisateur | Courriel          | Mot de passe  | Rôle       |
|-------------------|-------------------|---------------|------------|
| bob               | bob@gmail.com     | password123   | Utilisateur |
| anna              | anna@gmail.com    | password123   | Utilisateur |
| Admin             | admin@gmail.com   | password123   | Admin      |


Commande pour tester Login:
- Invoke-RestMethod -Uri "http://localhost:8080/auth/connexion" -Method POST -Headers @{"Content-Type"="application/json"} -Body '{"email": "bob@gmail.com", "password": "password123"}'

Commande pour tester Inscription:
- Invoke-RestMethod -Uri "http://localhost:8080/auth/inscription" -Method POST -Body '{"username":"Charlie","email":"charlie@gmail.com","password":"password123","confirmerPassword":"password123"}' -ContentType "application/json"

Commande pour tester Utilisateur/Detail
-  Invoke-RestMethod -Uri "http://localhost:8080/utilisateur/details" -Headers @{Authorization="Bearer TOKEN_ICI"}

Commande pour supprimer son propre compte:
- Invoke-RestMethod -Uri "http://localhost:8080/2" -Method DELETE -Headers @{Authorization="Bearer TOKEN_ICI"}

Commande pour modifier son compte:
- Invoke-RestMethod -Uri "http://localhost:8080/2" -Method POST -Headers @{Authorization="Bearer TOKEN_ICI"} -Form @{ email="nouveau_email@gmail.com"; password="nouveau_mot_de_passe" }

Commande pour modifier un rôle d'un user quand ADMIN:
- curl -X PUT "http://localhost:8080/1/role?nouveauRole=ROLE_ADMIN" -H "Authorization: Bearer TOKEN_ICI"



Ben:
- Possibilité de créer un compte
- Le profil de l’utilisateur doit inclure une photo de profil.
- L’utilisateur aura un profil dans lequel il pourra mettre ses
  informations personnelles à jour.
- Un utilisateur doit également pouvoir supprimer entièrement son
  compte.
- Possibilité de se connecter. Lors de la connexion, un JWT est
  retourné pour permettre d’authentifier les prochaines requêtes.
- Accorder ou retirer les privilèges d’administrateur à un autre utilisateur.
- Voir les commentaires qui ont été rapportés comme indésirables et
  potentiellement les supprimer en cas de besoin.
- Téléverser des fichiers

John:
- Un utilisateur doit valider son courriel pour être en mesure de se
connecter.
- La récupération/réinitialisation de mot de passe doit être
possible.
- Téléverser des fichiers 
- Télécharger les fichiers auxquels il a accès
- Affichage des fichiers
- Affichage des commentaires
- Ajout de commentaire


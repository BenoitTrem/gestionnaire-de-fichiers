const apiUrl = "http://localhost:8080";

/**
 * @author Benoit
 *
 * Récupère la soumission du formulaire d'inscription.
 */
document.getElementById("FormInscription").addEventListener("submit", async (e) => {
    e.preventDefault();

    // Nettoie des champs du formulaire.
    const username = document.getElementById("username").value.trim();
    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const confirmerPassword = document.getElementById("confirmerPassword").value.trim();
    const profileImage = document.getElementById("profileImage").files[0];

    // Vérifie que les mots de passe sont identiques.
    if (password !== confirmerPassword) {
        afficherErreur("Les mots de passe ne correspondent pas");
        return;
    }

    //Création d'un FormData pour envoyer le fichier + les autres champs.
    try {
        const formData = new FormData();
        formData.append("username", username);
        formData.append("email", email);
        formData.append("password", password);
        formData.append("confirmerPassword", confirmerPassword);

        // Ajout du fichier seulement si l'utilisateur en a sélectionné un.
        if (profileImage) {
            formData.append("profileImage", profileImage);
        }

        // Envoi de la requête d'inscription au backend.
        const response = await fetch(`${apiUrl}/auth/inscription`, {
            method: "POST",
            body: formData
        });

        const data = await response.text();

        // Si la réponse est une erreur, déclenche l'affichage d'erreur.
        if (!response.ok) {
            throw new Error(data);
        }

        alert("Inscription réussie ! Veuillez vérifier votre e-mail pour confirmer votre courriel.");
        window.location.href = "connexion.html?message=compte_cree";

    } catch (err) {
        console.error("Erreur de l'inscription:", err);
        afficherErreur(err.message || "Erreur lors de l'inscription");
    }
});

/**
 * @author Benoit
 *
 * Affiche un message d'erreur en haut du formulaire d'inscription.
 */
function afficherErreur(message) {
    let erreur = document.getElementById("ErreurInscription");
    if (!erreur) {
        erreur = document.createElement("div");
        erreur.id = "ErreurInscription";
        erreur.className = "text-danger mt-2";
        document.getElementById("FormInscription").prepend(erreur);
    }
    erreur.textContent = message;
}



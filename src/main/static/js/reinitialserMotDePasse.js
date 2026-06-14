const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get("token");

/**
 * Fonction d'affichage des messages d'erreurs
 * @param message
 */
function afficherErreur(message) {
    const errorMsg = document.getElementById("errorMsg");
    if (errorMsg) errorMsg.textContent = message;
}

/**
 * @author Benoit
 *
 * Gestion du formulaire pour mot de passe oublié
 * @type {HTMLElement}
 */
const MotDePasseForm = document.getElementById("mot_de_passe_oublie_form");
if (MotDePasseForm) {
    MotDePasseForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        afficherErreur("");

        // Récupération du champ email.
        const emailInput = document.getElementById("email");
        if (!emailInput) return;
        const email = emailInput.value.trim();

        if (!email) {
            afficherErreur("Veuillez entrer votre email.");
            return;
        }

        try {
            // Envoi de la requête au backend.
            const response = await fetch(`http://localhost:8080/MotDePasse?email=${encodeURIComponent(email).replace('%40', '@')}`, {
                method: "POST"
            });
            // Envoi de la requête au backend

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text);
            }

            alert("Un email pour réinitialiser votre mot de passe a été envoyé.");
            window.location.href = "../vues/Connexion.html";

        } catch (err) {
            afficherErreur(err.message);
        }
    });
}


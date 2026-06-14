const apiUrl = "http://localhost:8080";
const token = localStorage.getItem("accessToken");
const utilisateurId = localStorage.getItem("userId");

/**
 * @author Benoit
 *
 * Charge les informations du profil.
 */
document.addEventListener("DOMContentLoaded", async () => {
    try {
        // Récupère les détails de l'utilisateur authentifié.
        const response = await fetchWithAuth(`${apiUrl}/utilisateur/details`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error("Impossible de charger les détails de l'utilisateur");
        }

        const utilisateur = await response.json();

        // Remplit les champs du formulaire.
        const emailInput = document.getElementById("email");
        document.getElementById("username").value = utilisateur.username;
        emailInput.value = utilisateur.email;

        emailInput.setAttribute("data-old", utilisateur.email);

        // Gestion de l'affichage de la photo de profil.
        const div = document.getElementById("profil");

        let profil = `<div class="text-center mb-4">`;

        if (utilisateur.photoProfile) {
            let image = utilisateur.photoProfile.nomStocke;
            let imageUrl = `${apiUrl}${image}`;

            // Charge l'image via fetchWithAuth
            const imageResponse = await fetchWithAuth(imageUrl, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`
                }
            });

            if (imageResponse.ok) {
                const imageBlob = await imageResponse.blob();
                const imageObjectURL = URL.createObjectURL(imageBlob);

                profil += `
                    <img src="${imageObjectURL}" 
                         alt="${utilisateur.photoProfile.nomOriginal}" 
                         class="img-fluid rounded-circle" 
                         style="object-fit: cover; width: 200px; height: 200px;">
                `;
            } else {
                profil += `<p>Impossible de charger l'image de profil.</p>`;
            }
        } else {
            profil += `<p>Aucune photo de profil.</p>`;
        }

        div.innerHTML = profil;

    } catch (error) {
        console.error("Erreur lors du chargement du profil:", error);
        alert(error.message);
        window.location.href = "../vues/Index.html";
    }
});

/**
 * @author Benoit
 *
 * Envoie les modifications du compte utilisateur.
 */
document.getElementById("modifierCompteForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    // Récupère les valeurs du formulaire.
    const emailInput = document.getElementById("email");
    const oldEmail = emailInput.getAttribute("data-old");
    const username = document.getElementById("username").value;
    const email = emailInput.value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;
    const photoProfile = document.getElementById("photoProfile").files[0];

    // Vérifie que les mots de passes sont identiques.
    if (password !== confirmPassword) {
        afficherErreurModifier("Les mots de passe ne correspondent pas.");
        return;
    }

    // Prépare les données à envoyer.
    const formData = new FormData();
    formData.append("username", username);
    formData.append("email", email);
    if (password) formData.append("password", password);
    if (photoProfile) formData.append("photoProfile", photoProfile);

    try {
        // Requête de mise à jour.
        const response = await fetchWithAuth(`${apiUrl}/utilisateur/${utilisateurId}`, {
            method: "POST",
            headers: { "Authorization": `Bearer ${token}` },
            body: formData
        });

        const data = await response.json();

        if (!response.ok) {
            afficherErreurModifier(data.message || "Une erreur est survenue.");
            return;
        }

        alert(data.message);

        // Si l'email est modifié, l'utilisateur est redirigé vers la vue connexion.
        if (oldEmail && email !== oldEmail) {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("userId");

            alert("Votre email a été modifié. Veuillez vous reconnecter.");
            window.location.href = "../vues/Connexion.html";
            return;
        }

        window.location.href = "../vues/Compte.html";

    } catch (error) {
        console.error("Erreur lors de la modification du profil:", error);
        afficherErreurModifier("Erreur lors de la mise à jour du profil.");
    }
});

/**
 * @author Benoit
 *
 * Affiche un message d'erreur pour la modification du profil
 */
function afficherErreurModifier(message) {
    let erreur = document.getElementById("ErreurModifier");
    if (!erreur) {
        erreur = document.createElement("div");
        erreur.id = "ErreurModifier";
        erreur.className = "text-danger mt-2";
        document.getElementById("modifierCompteForm").prepend(erreur);
    }
    erreur.textContent = message;
}


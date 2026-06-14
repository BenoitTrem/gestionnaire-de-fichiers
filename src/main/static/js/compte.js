const apiUrl = "http://localhost:8080";
const token = localStorage.getItem("accessToken");
const utilisateurId = localStorage.getItem("userId");
const roles = JSON.parse(localStorage.getItem("roles") || "[]");
const estAutorise = roles.includes("ROLE_ADMIN") || roles.includes("ROLE_USER");

/**
 * @author Benoit
 *
 * Charge les informations du profil utilisateur.
 */
document.addEventListener("DOMContentLoaded", async () => {
    try {

        // Requête pour récupérer les détails du compte.
        const response = await fetchWithAuth(`${apiUrl}/utilisateur/details`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
                "Authorization": `Bearer ${token}`,
            }
        });

        if (!response.ok) {
            throw new Error(`Erreur lors du chargement du profil: ${response.status}`);
        }

        const data = await response.json();

        const div = document.getElementById("profil");

        let profil = `
            <h3 class="text-center mb-4">Détail du profil</h3>
            <div class="text-center mb-4">
        `;

        if (data.photoProfile) {
            let image = data.photoProfile.nomStocke;
            let imageUrl = `${apiUrl}${image}`;

            // Charge l'image via fetchWithAuth
            const imageResponse = await fetchWithAuth(imageUrl, {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                }
            });

            if (imageResponse.ok) {
                const imageBlob = await imageResponse.blob();
                const imageObjectURL = URL.createObjectURL(imageBlob);

                profil += `
                    <img src="${imageObjectURL}" alt="${data.photoProfile.nomOriginal}" class="img-fluid rounded-circle" style="object-fit: cover; width: 200px; height: 200px;">
                `;
            } else {
                profil += `<p>Impossible de charger l'image de profil.</p>`;
            }
        } else {
            profil += `<p>Aucune image de profil.</p>`;
        }

        // Traduction des rôles.
        const typeRole = {
            "ROLE_ADMIN": "Administrateur",
            "ROLE_USER": "Utilisateur"
        };

        const roleUtilisateur = data.roles.map(role => typeRole[role] || role);

        // Informations du profil.
        profil += `
                </div>
                <p><strong>Nom d'utilisateur:</strong> ${data.username}</p>
                <p><strong>Courriel:</strong> ${data.email}</p>
                <p><strong>Rôle:</strong> ${roleUtilisateur.join(", ")}</p> 
        `;

        profil += `
            <div class="d-flex gap-3 justify-content-end mt-4">
                <button id="boutonModifier" class="btn btn-outline-primary rounded-pill px-4 py-2 bold-outline-btn">Modifier mon compte</button>
                <button id="boutonSuppression" class="btn btn-outline-danger rounded-pill px-4 py-2 bold-outline-btn">Supprimer mon compte</button>
            </div>
        `;

        div.innerHTML = profil;

        // Suppression du compte.
        document.getElementById("boutonSuppression").addEventListener("click", async () => {
            const confirmation = confirm("Êtes-vous sûr de vouloir supprimer votre compte ?");
            if (!confirmation) {
                return;
            }

            // Retourne une erreur si l'utilisateur est inexistant
            if (!utilisateurId || utilisateurId === "null") {
                alert("Le ID de l'utilisateur est manquant.");
                return;
            }
            try {
                const response = await fetchWithAuth(`${apiUrl}/${utilisateurId}`, {
                    method: "DELETE",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${token}`
                    }
                });

                if (!response.ok) {
                    const errorData = await response.json();
                    console.error("Erreur lors de la suppression du compte:", errorData);
                    throw new Error(`Erreur lors de la suppression du compte: ${response.status} - ${errorData.message}`);
                }

                localStorage.clear();
                window.location.href = "../vues/Connexion.html";
            } catch (error) {
                console.error("Erreur lors de la suppression du compte:", error);
                alert("Erreur lors de la suppression du compte: " + error.message);
            }
        });

        // Redirection vers la page de modification.
        document.getElementById("boutonModifier").addEventListener("click", () => {
            window.location.href = "../vues/ModifierCompte.html";
        });

    } catch (error) {
        console.error("Erreur lors du chargement du profil:", error);
        alert(error.message);
        window.location.href = "../vues/Index.html";
    }
});

/**
 * @author Benoit
 *
 * Vérifie les permissions et affiche un message si l'utilisateur n'est pas autorisé */
window.onload = () => {
    const container = document.querySelector(".container");

    if (!estAutorise) {
        const unauthorizedMessage = `
            <div class="text-center mt-5">
                <h2 class="text-danger">Non autorisé</h2>
                <p class="text-blanc mt-2">Vous n'êtes pas autorisé à voir cette page.</p>
                <a href="../vues/Index.html" class="btn btn-primary mt-3">Retour</a>
            </div>
        `;
        container.innerHTML += unauthorizedMessage;
        return;
    }
};

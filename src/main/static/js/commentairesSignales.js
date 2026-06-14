const apiUrl = "http://localhost:8080";
const token = localStorage.getItem("accessToken");
const roles = JSON.parse(localStorage.getItem("roles") || "[]");
const estAutorise = roles.includes("ROLE_ADMIN");

/**
 * @author Benoit
 *
 * Fonction qui récupère les commentaires signalés
 * @returns {Promise<void>}
 */
async function fetchCommentairesSignales() {
    try {

        const response = await fetchWithAuth(`${apiUrl}/files/commentaires/signales`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        // Si la requête échoue.
        if (!response.ok) {
            throw new Error("Erreur lors de la récupération des commentaires signalés");
        }

        const commentaires = await response.json();
        const commentairesContainer = document.getElementById("commentairesTableBody");

        commentairesContainer.innerHTML = '';

        // Si aucun commentaire est signalé.
        if (commentaires.length === 0) {
            commentairesContainer.innerHTML = '<tr><td colspan="3" class="text-center">Aucun commentaire signalé.</td></tr>';
        }

        // Parcours de la liste des commentaires.
        commentaires.forEach(commentaire => {
            const commentaireElement = document.createElement("tr");

            // Vérifie si le commentaire est long (plus de 100 caractères).
            const commentaireLong = commentaire.texte.length > 100;
            const commentaireTextClass = commentaireLong ? "long" : "";

            commentaireElement.innerHTML = `
                <td>${commentaire.nomAuteur || "Auteur inconnu"}</td>
                <td>
                    <div class="comment-text ${commentaireTextClass}">
                        ${commentaireLong ? commentaire.texte.substring(0, 100) + '... <a href="#" class="more-btn">Voir plus</a>' : commentaire.texte}
                    </div>
                </td>
                <td class="text-center">
                    <button class="btn btn-outline-danger rounded-pill px-4 py-2 bold-outline-btn-commentaire" data-id="${commentaire.id}">Supprimer</button>
                </td>
            `;

            commentairesContainer.appendChild(commentaireElement);

            // Bouton qui ouvre un modal avec le texte complet.
            const voirPlusBtns = commentaireElement.querySelectorAll(".more-btn");
            voirPlusBtns.forEach(voirPlusBtn => {
                voirPlusBtn.addEventListener("click", (e) => {
                    e.preventDefault();

                    let commentaireComplet = commentaire.texte;
                    try {
                        const parsedCommentaire = JSON.parse(commentaire.texte);
                        commentaireComplet = JSON.stringify(parsedCommentaire, null, 2);
                    } catch (error) {
                        commentaireComplet = commentaire.texte;
                    }

                    document.getElementById("commentaireCompletTexte").textContent = commentaireComplet;

                    const modal = new bootstrap.Modal(document.getElementById('commentaireModal'));
                    modal.show();
                });
            });

            //supprime un commentaire signalé.
            const supprimerBtns = commentaireElement.querySelectorAll(".bold-outline-btn-commentaire");
            supprimerBtns.forEach(supprimerBtn => {
                supprimerBtn.addEventListener("click", supprimerCommentaire);
            });
        });
    } catch (error) {
        console.error(error);
        alert(error.message);
        window.location.href = "../vues/Index.html";
    }
}

/**
 * @author Benoit
 *
 * Fonction de suppression d'un commentaire
 *
 * @param event
 * @returns {Promise<void>}
 */
async function supprimerCommentaire(event) {
    // Récupère l’ID du commentaire depuis l’attribut data-id.
    const commentId = event.target.getAttribute("data-id");

    const isConfirmed = confirm("Êtes-vous sûr de vouloir supprimer ce commentaire ?");
    if (!isConfirmed) return;

    console.log(`Trying to delete comment with ID: ${commentId}`);
    try {
        const response = await fetchWithAuth(`${apiUrl}/files/commentaires/supprimer/${commentId}`, {
            method: "DELETE",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        // Si suppression OK.
        if (response.ok) {
            alert("Commentaire supprimé avec succès.");
            window.location.reload();
        } else {
            throw new Error("Erreur lors de la suppression du commentaire.");
        }
    } catch (error) {
        console.error(error);
        alert(error.message);

    }
}

/**
 * Vérification du rôle lors du chargement de la page
 */
window.onload = () => {
    const container = document.querySelector(".container");

    if (!estAutorise) {
        container.innerHTML = `
            <div class="text-center mt-5">
                <h2 class="text-danger">Non autorisé</h2>
                <p class="text-blanc mt-2">Vous n'avez pas la permission d'accéder à cette page.</p>
                <a href="../vues/Index.html" class="btn btn-primary mt-3">Retour</a>
            </div>
        `;
        return;
    }
    fetchCommentairesSignales();
};

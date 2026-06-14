/**
 * @file fichiersPublics.js
 * @description Gère l'affichage des fichiers publics/partagés, leurs commentaires,
 * le téléchargement, le signalement et le rafraîchissement automatique du token JWT.
 * @author John
 */

const apiUrl = "http://localhost:8080/files";
const token = localStorage.getItem("accessToken");
const email = localStorage.getItem("email");

/**
 * Charge la liste des fichiers depuis l’API et les affiche.
 * @async
 * @returns {void}
 * @author John
 */
async function fetchFichiers() {
    try {
        const response = await fetchWithAuth(`${apiUrl}/dto`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error("Erreur lors du chargement des fichiers");

        const fichiers = await response.json();
        afficherFichiers(fichiers);
    } catch (err) {
        console.error(err);
        alert("Erreur de chargement des fichiers");
        window.location.href = "../vues/Connexion.html";
    }
}

/**
 * Affiche les fichiers dans le DOM avec leurs commentaires et boutons d’action.
 * @param {Array<Object>} fichiers - Liste des fichiers à afficher.
 * @returns {void}
 * @author John
 */
function afficherFichiers(fichiers) {
    const container = document.getElementById("fichiersContainer");
    container.innerHTML = "";

    fichiers.forEach(fichier => {
        const commentairesHtml = fichier.commentaires.map(c =>
            `<div class="commentaire-item">
                <strong>${c.nomAuteur}</strong>
                <hr>
                <p class="mt-2">${c.texte}</p>
                <div class="d-flex justify-content-end">
                    <button class="btn btn-sm btn-signaler signal-btn mt-3" data-id="${c.id}">Signaler</button>
                </div>
            </div>`
        ).join("");

        const inputCommentaire = fichier.commentairesAutorises
            ? `<div class="comment-input">
                <label for="input-${fichier.idfichier}" class="form-label mb-3">Ajouter un commentaire :</label>
                <textarea class="form-control" id="input-${fichier.idfichier}" placeholder="Votre commentaire..."></textarea>
                <button class="btn btn-publier mt-3" onclick="envoyerCommentaire(${fichier.idfichier})">Publier</button>
            </div>`
            : `<p class="text-secondary fst-italic">Les commentaires sont désactivés pour ce fichier.</p>`;

        container.innerHTML += `
            <div class="card mb-4 container_">
                <div class="card-body">
                    <h5 class="card-title">${fichier.nom}</h5>
                    <p class="card-subtitle text-muted">Propriétaire : ${fichier.nomProprio}</p>
                    <div class="mt-3">
                        <button class="btn btn-sm btn-telecharger" onclick="downloadFile(${fichier.idfichier})">Télécharger</button>
                        <button class="btn btn-sm btn-commentaire ms-2" data-bs-toggle="modal" data-bs-target="#commentsModal-${fichier.idfichier}">
                            Commentaires
                        </button>
                    </div>
                </div>
            </div>
        `;

        const existingModal = document.getElementById(`commentsModal-${fichier.idfichier}`);
        if (existingModal) existingModal.remove();

        const modalHtml = `
        <div class="modal fade" id="commentsModal-${fichier.idfichier}" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered modal-xl">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">Commentaires pour ${fichier.nom}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        ${inputCommentaire}
                        <div class="comments-list">
                            ${commentairesHtml || '<p class="text-secondary">Aucun commentaire.</p>'}
                        </div>
                    </div>
                </div>
            </div>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', modalHtml);
    });

    activerSignalement();
}

/**
 * Active les boutons permettant d’afficher ou masquer les blocs de commentaires.
 * @returns {void}
 * @author John
 */
function activerToggleCommentaires() {
    const boutons = document.querySelectorAll(".toggle-comments-btn");
    boutons.forEach(btn => {
        btn.addEventListener("click", () => {
            const id = btn.getAttribute("data-id");
            const bloc = document.getElementById(`comments-${id}`);
            bloc.classList.toggle("d-none");
        });
    });
}

/**
 * Active les boutons de signalement de commentaires et gère l’envoi des signalements.
 * @async
 * @returns {void}
 * @author John
 */
function activerSignalement() {
    const boutons = document.querySelectorAll(".signal-btn");
    boutons.forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.getAttribute("data-id");

            if (!confirm("Signaler ce commentaire ?")) return;

            try {
                const res = await fetchWithAuth(`${apiUrl}/SignalerCommentaire/${id}`, {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                        "Content-Type": "application/json"
                    }
                });

                alert(res.ok ? "Commentaire signalé." : "Erreur lors du signalement.");
            } catch (e) {
                console.error(e);
                alert("Erreur côté client.");
            }
        });
    });
}

/**
 * Télécharge un fichier depuis le serveur via son ID.
 * @async
 * @param {number} id - Identifiant du fichier à télécharger.
 * @returns {void}
 * @author John
 */
async function downloadFile(id) {
    try {
        const response = await fetchWithAuth(`${apiUrl}/${id}`, { method: "GET" });

        if (!response.ok) {
            console.error(await response.text());
            alert("Téléchargement impossible (pas d’accès)");
            return;
        }

        const blob = await response.blob();
        const downloadUrl = URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = downloadUrl;
        const filename = response.headers
            .get("Content-Disposition")
            ?.split("filename=")[1]
            ?.replaceAll("\"", "") || "fichier";
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
    } catch (err) {
        console.error(err);
        alert("Erreur download");
    }
}

/**
 * Envoie un commentaire pour un fichier donné.
 * @async
 * @param {number} fileId - Identifiant du fichier commenté.
 * @returns {void}
 * @author John
 */
async function envoyerCommentaire(fileId) {
    const input = document.getElementById(`input-${fileId}`);
    const texte = input.value.trim();
    if (texte === "") return;

    const payload = { FichierId: fileId, texte };

    try {
        const response = await fetchWithAuth(`${apiUrl}/commenter`, {
            method: "POST",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            alert("Erreur lors du commentaire.");
            return;
        }

        alert("Commentaire ajouté.");
        const commentsList = document.querySelector(`#commentsModal-${fileId} .comments-list`);
        commentsList.insertAdjacentHTML('beforeend', `
            <div class="commentaire-item">
                <strong>Vous</strong>
                <hr>
                <p class="m-0">${texte}</p>
                <div class="d-flex justify-content-end">
                    <button class="btn btn-sm btn-signaler signal-btn mt-3" data-id="new">Signaler</button>
                </div>
            </div>
        `);

        activerSignalement();
        input.value = "";
    } catch (err) {
        console.error(err);
        alert("Erreur lors de l'envoi du commentaire");
    }
}

/**
 * Rafraîchit le token d’accès (JWT) via le serveur.
 * @async
 * @returns {Promise<string|undefined>} Nouveau token d’accès ou `undefined` si échec.
 * @author John
 */
async function refreshAccessToken() {
    try {
        const response = await fetch("http://localhost:8080/auth/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok)
            throw new Error("Une erreur est survenue. Veuillez vous reconnecter.");

        const data = await response.json();
        localStorage.setItem("accessToken", data.accessToken);
        return data.accessToken;
    } catch (err) {
        console.error("Error refreshing token:", err);
        window.location.href = "../vues/Connexion.html";
    }
}

/**
 * Fait une requête HTTP avec authentification JWT automatique + refresh si expiré.
 * @async
 * @param {string} url - L’URL de la requête.
 * @param {Object} [options={}] - Options du fetch (méthode, headers, body, etc.).
 * @returns {Promise<Response>} Réponse de la requête.
 * @author John
 */
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("accessToken");
    const headers = { ...options.headers };
    const isFormData = options.body instanceof FormData;

    if (!isFormData && options.body)
        headers["Content-Type"] = "application/json";

    if (token)
        headers["Authorization"] = `Bearer ${token}`;

    const response = await fetch(url, { ...options, headers, credentials: "include" });

    if (response.status === 401 && token) {
        const newToken = await refreshAccessToken();
        if (!newToken) throw new Error("Peut pas refresh le token");

        headers["Authorization"] = `Bearer ${newToken}`;
        return await fetch(url, { ...options, headers, credentials: "include" });
    }

    return response;
}

/**
 * charge automatiquement les fichiers à l’ouverture de la page.
 * @returns {void}
 * @author John
 */
window.onload = fetchFichiers;

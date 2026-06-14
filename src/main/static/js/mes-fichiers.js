/**
 * @file gestionFichiers.js
 * @description Gère le chargement, l’affichage et la gestion des fichiers de l’utilisateur :
 * upload, suppression, contributeurs, visibilité et commentaires.
 * Fournit aussi un système d’authentification JWT avec refresh automatique.
 * @author John
 */

const apiUrl = "http://localhost:8080/files";

/**
 * Charge les fichiers appartenant à l'utilisateur connecté.
 * @async
 * @returns {void}
 * @author John
 */
async function chargerMesFichiers() {
    try {
        const res = await fetchWithAuth(`${apiUrl}/mesfichiers`);
        if (!res.ok) throw new Error("Erreur de chargement");

        const fichiers = await res.json();
        afficherMesFichiers(fichiers);

    } catch (err) {
        console.error(err);
        alert("Impossible de charger vos fichiers.");
        window.location.href = "../vues/Index.html";
    }
}

/**
 * Affiche les fichiers de l'utilisateur avec les boutons d’action.
 * @param {Array<Object>} fichiers - Liste des fichiers à afficher.
 * @returns {void}
 * @author John
 */
function afficherMesFichiers(fichiers) {
    const cont = document.getElementById("mesFichiersContainer");
    cont.innerHTML = "";

    fichiers.forEach(f => {
        const divId = `contrib-zone-${f.idfichier}`;
        cont.innerHTML += `
        <div class="card mb-3 p-3 bg-dark container_ text-light">
            <h5>${f.nom}</h5>
            <p>Type : ${f.mimeType}</p>

            <div class="btn-group mb-3">
                <button class="btn btn-success btn-sm" onclick="downloadFile(${f.idfichier})">Télécharger</button>
                <button class="btn btn-warning btn-sm" onclick="toggleVisibility(${f.idfichier})">
                    ${f.visible ? "Rendre privé" : "Rendre public"}
                </button>
                <button class="btn btn-info btn-sm" onclick="toggleCommentaires(${f.idfichier})">
                    ${f.commentairesAutorises ? "Désactiver commentaires" : "Activer commentaires"}
                </button>
                <button class="btn btn-secondary btn-sm" onclick="toggleContributeurs(${f.idfichier})">Contributeurs</button>
                <button class="btn btn-danger btn-sm" onclick="supprimerFichier(${f.idfichier})">Supprimer</button>
            </div>

            <div id="${divId}" class="border-top pt-3 mt-3" style="display:none;">
                <h6 class="text-success">Contributeurs</h6>
                <ul id="liste-${f.idfichier}" class="list-group mb-3"></ul>

                <label class="text-light mt-2">Ajouter un contributeur :</label>
                <div class="input-group mb-2">
                    <select id="select-add-${f.idfichier}" class="form-select bg-dark text-light border-secondary">
                        <option value="">Chargement...</option>
                    </select>
                    <button class="btn btn-outline-success" onclick="ajouterContributeur(${f.idfichier})">Ajouter</button>
                </div>

                <label class="text-light mt-2">Retirer un contributeur :</label>
                <div class="input-group mb-2">
                    <select id="select-remove-${f.idfichier}" class="form-select bg-dark text-light border-secondary">
                        <option value="">Chargement...</option>
                    </select>
                    <button class="btn btn-outline-danger" onclick="retirerContributeur(${f.idfichier})">Retirer</button>
                </div>

                <p id="info-${f.idfichier}" class="text-warning mt-2"></p>
            </div>
        </div>`;
    });
}

/**
 * Affiche ou cache la zone des contributeurs d’un fichier.
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
function toggleContributeurs(id) {
    const zone = document.getElementById(`contrib-zone-${id}`);
    const isOpen = zone.style.display === "block";
    zone.style.display = isOpen ? "none" : "block";

    if (!isOpen) {
        chargerContributeurs(id);
        chargerSelectAjouter(id);
        chargerSelectRetirer(id);
    }
}

/**
 * Charge la liste des contributeurs d’un fichier.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function chargerContributeurs(id) {
    const ul = document.getElementById(`liste-${id}`);
    const info = document.getElementById(`info-${id}`);
    info.textContent = "";

    try {
        const res = await fetchWithAuth(`${apiUrl}/${id}/contributeurs`);
        if (!res.ok) {
            info.textContent = "Erreur lors du chargement.";
            return;
        }

        const data = await res.json();
        ul.innerHTML = "";
        data.forEach(user => {
            ul.innerHTML += `<li class="list-group-item bg-dark text-light">${user.username} (${user.email})</li>`;
        });

    } catch (e) {
        info.textContent = "Erreur serveur.";
        console.error(e);
    }
}

/**
 * Charge la liste des utilisateurs disponibles pour ajout en contributeur.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function chargerSelectAjouter(id) {
    const select = document.getElementById(`select-add-${id}`);
    select.innerHTML = `<option value="">Chargement...</option>`;

    const res = await fetchWithAuth(`${apiUrl}/${id}/utilisateurs-disponibles`);
    if (!res.ok) {
        select.innerHTML = `<option value="">Erreur serveur</option>`;
        return;
    }

    const users = await res.json();
    select.innerHTML = `<option value="">-- Sélectionner --</option>`;
    users.forEach(u => {
        select.innerHTML += `<option value="${u.email}">${u.username} (${u.email})</option>`;
    });
}

/**
 * Charge la liste des contributeurs pour suppression.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function chargerSelectRetirer(id) {
    const select = document.getElementById(`select-remove-${id}`);
    select.innerHTML = `<option value="">Chargement...</option>`;

    const res = await fetchWithAuth(`${apiUrl}/${id}/contributeurs`);
    if (!res.ok) {
        select.innerHTML = `<option value="">Erreur serveur</option>`;
        return;
    }

    const users = await res.json();
    select.innerHTML = `<option value="">-- Sélectionner --</option>`;
    users.forEach(u => {
        select.innerHTML += `<option value="${u.email}">${u.username} (${u.email})</option>`;
    });
}

/**
 * Ajoute un contributeur au fichier.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function ajouterContributeur(id) {
    const email = document.getElementById(`select-add-${id}`).value;
    const info = document.getElementById(`info-${id}`);
    if (!email) return alert("Veuillez choisir un utilisateur");

    const res = await fetchWithAuth(`${apiUrl}/${id}/contributeurs`, {
        method: "POST",
        body: JSON.stringify({ contributorEmail: email })
    });

    info.textContent = await res.text();
    chargerContributeurs(id);
    chargerSelectAjouter(id);
    chargerSelectRetirer(id);
}

/**
 * Retire un contributeur d’un fichier.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function retirerContributeur(id) {
    const email = document.getElementById(`select-remove-${id}`).value;
    const info = document.getElementById(`info-${id}`);
    if (!email) return alert("Veuillez choisir un contributeur");

    const res = await fetchWithAuth(`${apiUrl}/${id}/contributeurs`, {
        method: "DELETE",
        body: JSON.stringify({ contributorEmail: email })
    });

    info.textContent = await res.text();
    chargerContributeurs(id);
    chargerSelectAjouter(id);
    chargerSelectRetirer(id);
}

/**
 * Télécharge un fichier via son ID.
 * @async
 * @param {number} idfichier - Identifiant du fichier à télécharger.
 * @returns {void}
 * @author John
 */
async function downloadFile(idfichier) {
    try {
        const response = await fetchWithAuth(`${apiUrl}/${idfichier}`);
        if (!response.ok) return alert("Téléchargement impossible");

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;

        //pas fonctionel les fichier telechager montre toujours fichier donc l'option par defaut
        //
        const filename = response.headers.get("Content-Disposition")?.split("filename=")[1]?.replaceAll("\"", "") || "fichier";
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
 * Bascule la visibilité (public/privé) d’un fichier.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function toggleVisibility(id) {
    try {
        const res = await fetchWithAuth(`${apiUrl}/${id}/visibilite`, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());
        alert(await res.text());
        chargerMesFichiers();
    } catch (err) {
        console.error(err);
        alert("Erreur visibilité");
    }
}

/**
 * Active ou désactive les commentaires d’un fichier.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function toggleCommentaires(id) {
    try {
        const res = await fetchWithAuth(`${apiUrl}/${id}/commentaires`, { method: "POST" });
        if (!res.ok) throw new Error(await res.text());
        alert(await res.text());
        chargerMesFichiers();
    } catch (err) {
        console.error(err);
        alert("Erreur commentaires");
    }
}

/**
 * Supprime un fichier après confirmation.
 * @async
 * @param {number} id - Identifiant du fichier.
 * @returns {void}
 * @author John
 */
async function supprimerFichier(id) {
    if (!confirm("Supprimer ce fichier ?")) return;
    try {
        const res = await fetchWithAuth(`${apiUrl}/${id}`, { method: "DELETE" });
        if (!res.ok) throw new Error(await res.text());
        alert("Fichier supprimé");
        chargerMesFichiers();
    } catch (err) {
        console.error(err);
        alert("Erreur suppression");
    }
}

/**
 * Rafraîchit le token JWT (accessToken) à l’aide du refresh token.
 * @async
 * @returns {Promise<string|undefined>} Nouveau token d’accès ou `undefined` en cas d’échec.
 * @author John
 */
async function refreshAccessToken() {
    try {
        const response = await fetch("http://localhost:8080/auth/refresh", {
            method: "POST",
            credentials: "include"
        });

        if (!response.ok) throw new Error("Veuillez vous reconnecter.");
        const data = await response.json();
        localStorage.setItem("accessToken", data.accessToken);
        return data.accessToken;
    } catch (err) {
        console.error("Error refreshing token:", err);
        window.location.href = "../vues/Connexion.html";
    }
}

/**
 * Effectue une requête fetch avec authentification JWT et refresh automatique du token.
 * @async
 * @param {string} url - URL de la requête.
 * @param {Object} [options={}] - Options fetch standard.
 * @returns {Promise<Response>} Réponse HTTP.
 * @author John
 */
async function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem("accessToken");
    const headers = { ...options.headers };
    const isFormData = options.body instanceof FormData;

    if (!isFormData && options.body) headers["Content-Type"] = "application/json";
    if (token) headers["Authorization"] = `Bearer ${token}`;

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
 * Téléverse un nouveau fichier via un input type="file".
 * @async
 * @returns {void}
 * @author John
 */
async function uploadNewFile() {
    const fileInput = document.getElementById("uploadInput");
    const file = fileInput.files[0];
    if (!file) return alert("Choisis un fichier d’abord.");

    const formData = new FormData();
    formData.append("file", file);

    try {
        const res = await fetchWithAuth(apiUrl, { method: "POST", body: formData });
        if (!res.ok) throw new Error(await res.text());
        alert("Fichier téléversé !");
        chargerMesFichiers();
    } catch (e) {
        alert("Erreur upload");
        console.error(e);
    }
}

window.onload = () => {
    chargerMesFichiers();
    document.getElementById("uploadBtn").addEventListener("click", uploadNewFile);
};

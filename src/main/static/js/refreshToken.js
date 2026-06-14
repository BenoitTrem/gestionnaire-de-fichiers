/**
 * Rafraîchit le token d'accès en utilisant le refresh token (envoyé via cookie HTTPOnly).
 * Envoie une requête POST
 * Si le refresh fonctionne, l'accessToken dans le localStorage est remplacé
 * Si une erreur survient, l'utilisateur est redirigé vers la page de connexion
 * @returns {string|null} Le nouveau token ou null en cas d'erreur
 */
async function refreshAccessToken() {
    try {
        const response =  await fetch("http://localhost:8080/auth/refresh", {
            method: "POST",
            credentials: "include" // inclut le cookie contenant le refresh token.
        });

        if (!response.ok) {
            throw new Error("Une erreur est survenue. Veuillez vous reconnecter.");
        }

        // Récupère les données JSON (nouvel accessToken).
        const data = await response.json();

        // Stocke le nouveau token dans le localStorage.
        localStorage.setItem("accessToken", data.accessToken);
        return data.accessToken;
    } catch (err) {
        console.error("Error refreshing token:", err);
        window.location.href = "../vues/Connexion.html";
    }
}

/**
 * Effectue une requête fetch
 * Ajoute automatiquement le token dans les headers
 * Si la requête retourne 401, tente de rafraîchir le token
 * Réessaie la requête avec le nouveau token
 *
 * @param {string} url       L'URL de la requête
 * @param {object} options   Options fetch
 * @returns {Response}       La réponse fetch finale
 */
async function fetchWithAuth(url, options = {}) {
    // Récupère le token actuel.
    let token = localStorage.getItem("accessToken");

    const headers = {
        ...options.headers
    };

    // Vérifie si le body est un FormData.
    const isFormData = options.body instanceof FormData;
    if (!isFormData && options.body) {
        headers["Content-Type"] = "application/json";
    }

    // Ajoute le token si disponible.
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(url, {
        ...options,
        headers,
        credentials: "include"
    });

    // Si token expiré, tente de refresh le token.
    if (response.status === 401 && token) {
        const newToken = await refreshAccessToken();
        if (!newToken) throw new Error("Peut pas refresh le token");

        headers["Authorization"] = `Bearer ${newToken}`;

        // Réessaie la requête initiale avec le nouveau token.
        return await fetch(url, {
            ...options,
            headers,
            credentials: "include"
        });
    }

    return response;
}


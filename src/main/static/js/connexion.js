const apiUrl = "http://localhost:8080";

/**
 * @author Benoit
 *
 * Gestion de la soumission du formulaire de connexion.
 * Empêche le rechargement de la page, récupère l'email et le mot de passe,
 * envoie la requête au backend, puis stocke le token et les informations de  l'utilisateur.
 */
document.getElementById("FormConnexion").addEventListener("submit", async (e) => {
    e.preventDefault();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    try {
        const response = await fetch(`${apiUrl}/auth/connexion`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        // Si le backend renvoie une erreur.
        if (!response.ok) {
            throw new Error(data.message);
        }

        // Enregistre les données utilisateur dans le stockage local.
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("userEmail", data.email);
        localStorage.setItem("userId", data.userId);
        localStorage.setItem("roles", JSON.stringify(data.roles));

        // Redirection vers la page d'accueil
        window.location.href = "Index.html";

    } catch (err) {
        console.error("Erreur de connexion:", err);
        afficherErreur(err.message || "Erreur de connexion:");
    }
});

/**
 * @author Benoit
 *
 * Affiche un message d'erreur au-dessus du formulaire de connexion.
 */
function afficherErreur(message) {
    let erreur = document.getElementById("ErreurConnexion");
    if (!erreur) {
        erreur = document.createElement("div");
        erreur.id = "ErreurConnexion";
        erreur.className = "text-danger mt-2";
        document.getElementById("FormConnexion").prepend(erreur);
    }
    erreur.textContent = message;
}



/**
 * @author Benoit
 * Menu
 */
(() => {
    // Récupération des éléments du menu.
    const navLinks = document.getElementById("navLinks");
    const boutonGererUtilisateur = document.getElementById("bouton_gere_utilisateur");
    const boutonCommentaireIndiserable = document.getElementById("bouton_commetaire_indiserable");
    const boutonMesFichiers = document.getElementById("bouton_mes_fichiers");
    const boutonAccueil = document.getElementById("bouton_accueil");

    if (!navLinks || !boutonGererUtilisateur || !boutonCommentaireIndiserable || !boutonMesFichiers || !boutonAccueil) {
        console.error("Menu introuvable");
        return;
    }

    const token = localStorage.getItem("accessToken");
    const email = localStorage.getItem("userEmail");
    const roles = JSON.parse(localStorage.getItem("roles") || "[]");

    // Récupère la vue courante.
    const PageCourante = window.location.pathname.toLowerCase();

    // Les pages où les boutons suivants ne doivent pas apparaître.
    if (PageCourante.endsWith("inscription.html") || PageCourante.endsWith("connexion.html") ||
        PageCourante.endsWith("connexion.html?message=compte_cree") || PageCourante.endsWith("motdepasseoublie.html")) {
        boutonGererUtilisateur.style.display = 'none';
        boutonCommentaireIndiserable.style.display = 'none';
        boutonAccueil.style.display = 'none';
        boutonMesFichiers.style.display = 'none';
        return;
    }
    navLinks.innerHTML = `

    <li class="nav-item">
        <li class="nav-item">
            <a href="../vues/Compte.html" class="btn btn-outline-info rounded-pill px-4 py-2 bold-outline-btn" id="CompteButton">Compte</a>
        </li>
      
        <li class="nav-item">
            <a href="#" class="btn btn-outline-danger rounded-pill ms-2 px-4 py-2 bold-outline-btn" id="BoutonLogout">Déconnexion</a>
        </li>
    `;

    // Si l'utilisateur est un admin, les boutons sont affichés.
    if (token && roles.includes("ROLE_ADMIN")) {
        boutonGererUtilisateur.style.display = 'inline-block';
        boutonCommentaireIndiserable.style.display = 'inline-block';
    } else {
        boutonGererUtilisateur.style.display = 'none';
        boutonCommentaireIndiserable.style.display = 'none';
    }

    // Bouton pour la déconnexion
    document.getElementById("BoutonLogout").addEventListener("click", (e) => {
        e.preventDefault();
        localStorage.removeItem("accessToken");
        localStorage.removeItem("userEmail");
        localStorage.removeItem("roles");
        window.location.href = "../vues/Connexion.html";
    });

    // Gestion de l'affichage des nav item quand page sont actif ou non
    const mettre_a_jour_btn = () => {
        if (PageCourante.includes("listutilisateur.html")) {
            boutonGererUtilisateur.classList.add("active");
            boutonCommentaireIndiserable.classList.remove("active");
            boutonMesFichiers.classList.remove("active");
            boutonAccueil.classList.remove("active");
        } else if (PageCourante.includes("listecommentairessignales.html")) {
            boutonCommentaireIndiserable.classList.add("active");
            boutonGererUtilisateur.classList.remove("active");
            boutonMesFichiers.classList.remove("active");
            boutonAccueil.classList.remove("active");
        } else if (PageCourante.includes("mes-fichiers.html")) {
            boutonMesFichiers.classList.add("active");
            boutonGererUtilisateur.classList.remove("active");
            boutonCommentaireIndiserable.classList.remove("active");
            boutonAccueil.classList.remove("active");
        } else if (PageCourante.includes("index.html")) {
            boutonAccueil.classList.add("active");
            boutonGererUtilisateur.classList.remove("active");
            boutonCommentaireIndiserable.classList.remove("active");
            boutonMesFichiers.classList.remove("active");
        } else {
            boutonGererUtilisateur.classList.remove("active");
            boutonCommentaireIndiserable.classList.remove("active");
            boutonMesFichiers.classList.remove("active");

        }
    };
    mettre_a_jour_btn();
})();

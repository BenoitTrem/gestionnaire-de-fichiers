const apiUrl = "http://localhost:8080";
const token = localStorage.getItem("accessToken");
const roles = JSON.parse(localStorage.getItem("roles") || "[]");
const estAutorise = roles.includes("ROLE_ADMIN");

async function fetchUtilisateurs() {
    try {

        const response = await fetchWithAuth(`${apiUrl}/utilisateurs`, {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error('Erreur lors de la récupération des utilisateurs.');
        }

        const utilisateurs = await response.json();
        const tableBody = document.getElementById("listeUtilisateur");

        for (const utilisateur of utilisateurs) {
            const tableUtilisateur = document.createElement('tr');
            const role = utilisateur.roles.includes('ROLE_ADMIN') ? 'Administrateur' : 'Utilisateur';

            let imageContent = 'Aucune photo';
            if (utilisateur.photoProfile) {
                let photoPath = utilisateur.photoProfile.nomStocke;
                const imageUrl = `${apiUrl}${photoPath}`;

                const imageResponse = await fetchWithAuth(imageUrl, {
                    method: "GET",
                    headers: {
                        "Authorization": `Bearer ${token}`,
                    }
                });

                if (imageResponse.ok) {
                    const imageBlob = await imageResponse.blob();
                    imageContent = `<img src="${URL.createObjectURL(imageBlob)}" class="img-fluid rounded-circle" style="width: 50px; height: 50px; object-fit: cover;" />`;
                }
            }

            tableUtilisateur.innerHTML = `
                <td>${utilisateur.id}</td>
                <td class="text-center">${imageContent}</td>
                <td>${utilisateur.username}</td>
                <td>${utilisateur.email}</td>
                <td>${role}</td>
                <td>
                    <button class="btn btn-outline-primary rounded-pill px-4 py-2 bold-outline-btn" onclick="modifierRole(${utilisateur.id}, '${utilisateur.roles.includes('ROLE_ADMIN') ? 'ROLE_USER' : 'ROLE_ADMIN'}')">
                    ${utilisateur.roles.includes('ROLE_ADMIN') ? 'Promouvoir à Utilisateur' : 'Promouvoir à Admin'}
                </button>
                </td>
            `;
            tableBody.appendChild(tableUtilisateur);
        }

    } catch (error) {
        console.error(error);
        alert(error.message);
        window.location.href = "../vues/Index.html";
    }
}

async function modifierRole(utilisateurId, nouveauRole) {
    try {


        const response = await fetchWithAuth(`${apiUrl}/${utilisateurId}/role?nouveauRole=${nouveauRole}`, {
            method: "PUT",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (response.ok) {
            const data = await response.json();
            alert(data.message);
            location.reload();
        } else {
            const erreurData = await response.json();
            console.log("Erreur:", erreurData);
            alert(`Erreur: ${erreurData.message || 'Une erreur est survenue.'}`);
        }
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

window.onload = () => {
    const container = document.querySelector(".container");

    if (!estAutorise) {
        container.innerHTML = `
            <div class="text-center mt-5">
                <h2 class="text-danger">Non autorisé</h2>
                <p class="text-blanc mt-2">Vous n'avez pas la permission d'accéder à cette page.</p>
                <a href="../vues/Index.html" class="btn btn-primary mt-3
                ">Retour</a>
            </div>
        `;
        return;
    }

    fetchUtilisateurs();
};

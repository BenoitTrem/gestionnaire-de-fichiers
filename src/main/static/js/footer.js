/**
 * @author Benoit
 * @type {Date}
 */
const currentDate = new Date(); // Création d'un objet Date contenant la date actuelle
const formattedDate = currentDate.toLocaleDateString('fr-FR', { year: 'numeric' });
document.getElementById("date").textContent = formattedDate;

package org.example.projet_final.services;


import org.example.projet_final.models.Commentaire;
import org.example.projet_final.models.dto.CommentaireDTO;
import org.example.projet_final.repositories.CommentaireRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommentairesService {

    private final CommentaireRepository commentaireRepository;

    public CommentairesService(CommentaireRepository commentaireRepository) {
        this.commentaireRepository = commentaireRepository;
    }

    /**
     * @author John
     *
     *  Convertit un objet Commentaire en CommentaireDTO.
     *
     * @param commentaire Le commentaire à convertir.
     * @return Le CommentaireDTO contenant l'ID, le texte et le nom de l'auteur du commentaire.
     */
    public CommentaireDTO maptoDTO(Commentaire commentaire) {
        return new CommentaireDTO(
                commentaire.getId(),
                commentaire.getTexte(),
                commentaire.getAuteur().getUsername()
        );
    }
    /**
     * @author John
     *
     *  Signale un commentaire en le marquant comme "signalé".
     *  Si le commentaire est trouvé, sa propriété "signaler" est définie sur true et il est sauvegardé.
     *  Si le commentaire n'est pas trouvé, une exception est levée.
     *
     * @param id L'ID du commentaire à signaler.
     * @throws IllegalArgumentException Si le commentaire avec l'ID spécifié n'existe pas.
     */
    public void Signaler(Long id) {
        Optional<Commentaire> commentaire = commentaireRepository.findById(id);
        if (commentaire.isPresent()) {
            commentaire.get().setSignaler(true);
            commentaireRepository.save(commentaire.get());
        } else {
            throw new IllegalArgumentException("Commentaire not found");
        }
    }


    /**
     * @author Benoit
     * @return la liste de tous les commentaires signalés
     */
    public List<Commentaire> getCommentairesSignales() {
        return commentaireRepository.findBySignaler(true);
    }

    /**
     * @author Benoit
     *
     * Supprime un commentaire selon son identifiant
     * @param id Identifiant du commentaire à supprimer
     */
    public void supprimerCommentaire(Long id) {
        Commentaire commentaire = commentaireRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire non trouvé"));
        commentaireRepository.delete(commentaire);
    }
}

package org.example.projet_final.repositories;

import org.example.projet_final.models.Commentaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author Benoit
 */
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {
    public List<Commentaire> findBySignaler(boolean signaler);
}

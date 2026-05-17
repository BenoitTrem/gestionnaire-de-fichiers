package org.example.projet_final.repositories;

import org.example.projet_final.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Benoit
 */
public interface UtilisateurRepository  extends JpaRepository<Utilisateur, Integer> {
    Optional<Utilisateur> findById(Integer id);
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByEmail(String email);
    List<Utilisateur> findAll();
}

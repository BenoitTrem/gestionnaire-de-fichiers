package org.example.projet_final.repositories;

import org.example.projet_final.models.FichierProprietaire;
import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Benoit / John
 */
public interface FichierProprietaireRepository extends JpaRepository<FichierProprietaire,Long> {
    List<FichierProprietaire> findByProprietaire(Utilisateur proprietaire);
    Optional<FichierProprietaire> findByFileData(FileMetaData file);

    List<FichierProprietaire> findAllByProprietaire(Utilisateur proprietaire);
    Optional<FichierProprietaire> findByFileData_Id(Long fileId);

}

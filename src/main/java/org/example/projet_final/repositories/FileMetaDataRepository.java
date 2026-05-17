package org.example.projet_final.repositories;

import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * @author Benoit / John
 */
public interface FileMetaDataRepository extends JpaRepository<FileMetaData, Long> {
    Optional<FileMetaData> findByNomStocke(String nomStocke);
    Optional<FileMetaData> findFirstByNomStocke(String nomStocke);
    List<FileMetaData> findAllByProprietaire(Utilisateur utilisateur);

}

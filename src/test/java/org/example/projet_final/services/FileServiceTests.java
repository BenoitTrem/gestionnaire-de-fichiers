package org.example.projet_final.services;

import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.FichierProprietaire;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.repositories.FichierProprietaireRepository;
import org.example.projet_final.repositories.FileMetaDataRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 *  @author John
 ** Tests unitaires pour FileService.
 * Couvre les méthodes removeContributor et toggleCommentaires.
 */

@ExtendWith(MockitoExtension.class)
public class FileServiceTests {

    @Mock
    private FichierProprietaireRepository fichierProprietaireRepository;

    @Mock
    private FileMetaDataRepository repository;

    @InjectMocks
    private FileService fileService;

    private Utilisateur owner;
    private Utilisateur contributor;
    private FichierProprietaire fichierProprietaire;
    private FileMetaData fileMetaData;

    @BeforeEach
    void setUp() {
        owner = Utilisateur.builder()
                .id(1)
                .email("owner@gmail.com")
                .username("owner")
                .build();

        contributor = Utilisateur.builder()
                .id(2)
                .email("contrib@gmail.com")
                .username("contrib")
                .build();

        fichierProprietaire = FichierProprietaire.builder()
                .id(1L)
                .proprietaire(owner)
                .contributeurs(new HashSet<>(Set.of(contributor)))
                .build();

        fileMetaData = FileMetaData.builder()
                .id(1L)
                .commentairesAutorises(true)
                .proprietaire(fichierProprietaire)
                .build();
    }


    @Test
    public void FileService_removeContributor_EnleveContributeur() {

        when(repository.findById(1L))
                .thenReturn(Optional.of(fileMetaData));

        when(fichierProprietaireRepository.findByFileData(fileMetaData))
                .thenReturn(Optional.of(fichierProprietaire));

        fileService.removeContributor(1L, owner.getEmail(), contributor);

        // Vérifications
        assertThat(fichierProprietaire.getContributeurs()).doesNotContain(contributor);
        verify(fichierProprietaireRepository, times(1)).save(fichierProprietaire);
    }



    @Test
    public void FileService_toggleCommentaires_ToggleTrueToFalse() {

        when(repository.findById(1L)).thenReturn(Optional.of(fileMetaData));

        //faire le toggle/changement
        boolean newState = fileService.toggleCommentaires(1L, owner.getEmail());


        //verification
        assertFalse(newState);
        assertFalse(fileMetaData.isCommentairesAutorises());
        verify(repository, times(1)).save(fileMetaData);
    }
}

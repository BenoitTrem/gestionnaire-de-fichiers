package org.example.projet_final.services;

import org.example.projet_final.models.Commentaire;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.repositories.CommentaireRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @author Benoit
 */
@ExtendWith(MockitoExtension.class)
public class CommentairesServiceTests {
    @Mock
    private CommentaireRepository commentaireRepository;
    @InjectMocks
    private CommentairesService commentairesService;

    private Commentaire commentaire;
    private Utilisateur auteur;

    @BeforeEach
    void setUp() {
        auteur = Utilisateur.builder()
                .id(1)
                .username("user1")
                .build();

        // Création d'un commentaire.
        commentaire = Commentaire.builder()
                .id(1L)
                .texte("Test commentaire")
                .auteur(auteur)
                .signaler(false)
                .build();
    }

    /**
     * Test pour vérifier que la méthode Signaler fonctionne correctement.
     * Le commentaire doit être marqué comme signalé après l'appel du service.
     */
    @Test
    public void CommentairesService_Signaler_Commentaire_Reussi() {
        when(commentaireRepository.findById(1L)).thenReturn(Optional.of(commentaire));

        // Appel du service pour signaler le commentaire.
        commentairesService.Signaler(1L);

        // ArgumentCaptor permet de capturer l'objet passé à la méthode save pour vérifier ses valeurs.
        // J'ai utilisé ChatGPT pour générer cette solution.
        ArgumentCaptor<Commentaire> captor = ArgumentCaptor.forClass(Commentaire.class);
        verify(commentaireRepository).save(captor.capture()); // Vérifie que save a été appelé.

        Commentaire saved = captor.getValue(); // Récupère le commentaire sauvegardé.
        assertTrue(saved.isSignaler()); // Vérifie que le commentaire est bien signalé.
    }

    /**
     * Test pour vérifier que la méthode supprimerCommentaire fonctionne correctement.
     * Le commentaire doit être supprimé via le repository.
     */
    @Test
    public void CommentairesService_Supprimer_Commentaire_Reussi() {
        when(commentaireRepository.findById(1L)).thenReturn(Optional.of(commentaire));

        // Appel du service pour supprimer le commentaire.
        commentairesService.supprimerCommentaire(1L);

        // Vérifie que la méthode delete du repository a été appelée avec le commentaire correct.
        verify(commentaireRepository).delete(commentaire);
    }

}

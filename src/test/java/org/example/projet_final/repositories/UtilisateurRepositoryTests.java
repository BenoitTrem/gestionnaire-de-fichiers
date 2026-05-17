package org.example.projet_final.repositories;

import org.example.projet_final.models.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Benoit
 */
public class UtilisateurRepositoryTests {
    private UtilisateurRepository utilisateurRepository;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        utilisateurRepository = mock(UtilisateurRepository.class);

        // Création d'un utilisateur.
        utilisateur = Utilisateur.builder()
                .id(1)
                .username("test")
                .email("test@gmail.com")
                .password("encodedPass")
                .estVirifie(true)
                .build();

        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.findAll()).thenReturn(java.util.List.of(utilisateur));
    }

    /**
     * Test pour vérifier que la méthode findById fonctionne correctement.
     * Vérifie que l'utilisateur retourné a les bonnes informations.
     */
    @Test
    void utilisateurRepository_findByUsername() {
        Optional<Utilisateur> found = utilisateurRepository.findById(1);
        assertEquals(true, found.isPresent()); // Vérifie qu'un utilisateur est trouvé.
        assertEquals("test", found.get().getUsername()); // Vérifie le nom d'utilisateur.
        assertEquals("test@gmail.com", found.get().getEmail()); // Vérifie l'email.
    }

    /**
     * Test pour vérifier que la méthode findAll fonctionne correctement.
     * Vérifie que la liste retournée contient l'utilisateur simulé.
     */
    @Test
    void utilisateurRepository_findAll() {
        var allUsers = utilisateurRepository.findAll();
        assertEquals(1, allUsers.size()); // Vérifie qu'il y a un utilisateur dans la liste.
        assertEquals("test", allUsers.get(0).getUsername()); // Vérifie le nom d'utilisateur du premier élément.
    }
}


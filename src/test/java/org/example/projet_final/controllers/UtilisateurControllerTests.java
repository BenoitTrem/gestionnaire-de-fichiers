package org.example.projet_final.controllers;

import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.Role;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.services.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Set;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Benoit
 */
@WebMvcTest(controllers = UtilisateurController.class)
@AutoConfigureMockMvc
public class UtilisateurControllerTests {

    @Autowired
    MockMvc mockMvc; // Permet de simuler des requêtes HTTP vers le contrôleur.
    @MockitoBean
    UtilisateurService utilisateurService; // Mock du service utilisateur.

    Utilisateur utilisateur;
    Role userRole;
    FileMetaData  fileMetaData;

    /**
     * Configuration initiale exécutée avant chaque test.
     * Crée des objets utilisateur, rôle et fichier simulés.
     */
    @BeforeEach
    void setUp() {

        // Création d'un rôle.
        userRole = Role.builder()
                .id(1)
                .name("ROLE_USER")
                .build();

        // Création d'un fichier.
        fileMetaData = FileMetaData.builder()
                .id(1L)
                .nomOriginal("profileImage_1")
                .mimeType("image/png")
                .build();

        // Création d'un utilisateur.
        utilisateur = Utilisateur.builder()
                .id(5)
                .username("test")
                .email("test@gmail.com")
                .password("encodedPass")
                .roles(Set.of(userRole))
                .estVirifie(true)
                .photoProfile(fileMetaData)
                .build();
    }

    /**
     * Test pour vérifier que la récupération des détails d'un utilisateur fonctionne correctement.
     */
    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    public void UtilisateurController_get_Utilisateur_Details() throws Exception {

        when(utilisateurService.getUtilisateur()).thenReturn(utilisateur);

        // Envoie une requête GET sur "/utilisateur/details" et vérifie la réponse JSON
        mockMvc.perform(get("/utilisateur/details"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("test"))
                .andExpect(jsonPath("$.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.photoProfile.nomOriginal").value("profileImage_1"))
                .andExpect(jsonPath("$.photoProfile.nomStocke").value("/files/1"))
                .andExpect(jsonPath("$.photoProfile.mimeType").value("image/png"));
    }

    /**
     * Test pour vérifier qu'un utilisateur standard ne peut pas modifier son rôle.
     * Renvoie un statut 403 si l'utilisateur tente de changer son rôle.
     */
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    public void UtilisateurController_Utilisateur_Ne_Peut_Pas_Modifier_Role() throws Exception {
        mockMvc.perform(put("/2/role")
                        .param("nouveauRole", "ROLE_ADMIN")) // Nouveau rôle à attribuer.
                .andExpect(status().isForbidden());
    }



}

package org.example.projet_final.controllers;

import org.example.projet_final.models.FileMetaData;
import org.example.projet_final.models.Requests.ContributorRequest;
import org.example.projet_final.models.Role;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.models.dto.FichierDTO;
import org.example.projet_final.services.CommentairesService;
import org.example.projet_final.services.FileService;
import org.example.projet_final.services.UtilisateurService;
import org.example.projet_final.repositories.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @author John
 * Classe de test pour le contrôleur FileController.
 * Teste les endpoints de gestion des fichiers et des contributeurs.
 */
@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FileControllerTests {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UtilisateurRepository utilisateurRepository;

    @MockitoBean
    CommentairesService commentairesService;


    @MockitoBean
    FileService fileService;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    UtilisateurService utilisateurService;

    Utilisateur utilisateur;
    Role userRole;
    FileMetaData fileMetaData;
    FichierDTO fichierDTO;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .id(1)
                .name("ROLE_USER")
                .build();

        fileMetaData = FileMetaData.builder()
                .id(1L)
                .nomOriginal("profileImage_1.png")
                .mimeType("image/png")
                .build();

        utilisateur = Utilisateur.builder()
                .id(5)
                .username("test")
                .email("test@gmail.com")
                .password("encodedPass")
                .roles(Set.of(userRole))
                .estVirifie(true)
                .photoProfile(fileMetaData)
                .build();

        fichierDTO = FichierDTO.builder()
                .idfichier(1L)
                .nom("fichierTest.txt")
                .nomProprio("test")
                .commentairesAutorises(true)
                .build();
    }

    /**
     * vérifie que la route /files/mesfichiers retourne bien la liste
     * des fichiers appartenant à l’utilisateur connecté.
     */
    @Test
    public void fileController_getMesFichiers_RetourneListeFichiersUtilisateur() throws Exception {

        when(utilisateurService.getUtilisateur()).thenReturn(utilisateur);
        when(fileService.getFilesByAuteur(utilisateur)).thenReturn(List.of(fichierDTO));

        // Exécution de la requête et vérification du résultat
        mockMvc.perform(get("/files/mesfichiers")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk()) // Vérifie le statut HTTP
                .andExpect(jsonPath("$[0].nom").value("fichierTest.txt")) // Vérifie le contenu JSON
                .andExpect(jsonPath("$[0].nomProprio").value("test")); //Vérifie le nom du proprio
    }

    /**
     * vérifie que la route POST /files/{id}/contributeurs
     * ajoute correctement un contributeur à un fichier.
     */
    @Test
    public void fileController_addContributor_AjouteContributeurAvecSucces() throws Exception {
        Long fichierId = 1L;
        String contributorEmail = "ami@gmail.com";

        //CRéation de la request
        ContributorRequest request = new ContributorRequest();
        request.setContributorEmail(contributorEmail);

        //créer les users
        Utilisateur owner = utilisateur;
        Utilisateur contributor = Utilisateur.builder()
                .id(10)
                .username("ami")
                .email(contributorEmail)
                .password("pass")
                .roles(Set.of(userRole))
                .estVirifie(true)
                .build();

        when(utilisateurService.getUtilisateur()).thenReturn(owner);
        when(utilisateurService.getByEmail(contributorEmail)).thenReturn(contributor);
        doNothing().when(fileService).addContributor(fichierId, owner.getEmail(), contributor);

        mockMvc.perform(post("/files/{id}/contributeurs", fichierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "contributorEmail": "ami@gmail.com"
                            }
                        """))
                .andExpect(status().isOk())
                //Message de vrifiaction
                .andExpect(content().string("Contributeur ajouté avec succès"));

        //nombre de fois appelle
        verify(fileService, times(1)).addContributor(fichierId, owner.getEmail(), contributor);
    }








}

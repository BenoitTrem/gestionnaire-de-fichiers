package org.example.projet_final.repositories;

import org.example.projet_final.models.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author John
 * Classe de test pour le Repository RoleRepository
 *Tester le retour de tout les roles et role par Nom
 */
@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RoleRepositoryTests {

    @Autowired
    private RoleRepository roleRepository;


    @Test
    void testFindAll_RetourneTousLesRoles() {

        // Ajouter des rôles en base pour tester
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .build();

        roleRepository.save(adminRole);
        roleRepository.save(userRole);

        // Tester la méthode findAll
        List<Role> roles = roleRepository.findAll();

        // Vérifier qu'il y a deux rôles et qu'ils ont les bons noms
        assertThat(roles).hasSize(2);
        assertThat(roles).extracting(Role::getName)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void testFindByName_RetourneRoleParNom() {
        // Ajouter un rôle pour tester
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .build();
        roleRepository.save(adminRole);

        // Tester la méthode findByName
        Role role = roleRepository.findByName("ROLE_ADMIN");
        assertThat(role).isNotNull();
        assertThat(role.getName()).isEqualTo("ROLE_ADMIN");

        // Tester un rôle inexistant
        Role nonExistentRole = roleRepository.findByName("ROLE_UNKNOWN");
        assertThat(nonExistentRole).isNull();
    }
}

package org.example.projet_final.repositories;

import org.example.projet_final.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Benoit
 */
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByName(String name);
}

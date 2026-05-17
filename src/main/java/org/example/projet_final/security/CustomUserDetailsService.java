package org.example.projet_final.security;

import org.example.projet_final.models.Role;
import org.example.projet_final.models.Utilisateur;
import org.example.projet_final.repositories.UtilisateurRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author Benoit
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    /**
     * Constructeur injectant le repository des utilisateurs.
     * Permet de récupérer les données de l'utilisateur de la base de données.
     */
    public CustomUserDetailsService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Charge un utilisateur à partir de son courriel.
     * Utilisé par Spring Security lors de l'authentification.
     *
     * @param email courriel de l'utilisateur
     * @return UserDetails contenant les infos de connexion et les rôles
     * @throws UsernameNotFoundException si aucun utilisateur n'existe avec ce courriel
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Recherche de l'utilisateur via le courriel.
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur pas trouvé avec le courriel: " + email));

        // Construction d'un objet User reconnu par Spring Security.
        return User.withUsername(utilisateur.getEmail())
                .password(utilisateur.getPassword())
                .authorities(utilisateur.getRoles().stream()
                        .map(Role::getName) // Conversion des rôles en String.
                        .toArray(String[]::new))
                .build();
    }

}


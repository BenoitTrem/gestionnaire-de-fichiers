package org.example.projet_final.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * @author Benoit
 */
@Configuration
public class CorsGlobalConfig {

    /**
     * Cette configuration permet d'autoriser les requêtes provenant de "localhost",
     * ce qui nous a permis de tester l’application en local sans rencontrer d’erreurs CORS
     * entre le backend et le frontend.
     * J’ai utilisé ChatGPT pour m’aider à générer cette configuration afin
     * d’assurer que mes tests locaux fonctionnent correctement sans restrictions CORS.
     * @return un filtre CORS appliqué à toute l’application
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Autorise toutes les origines venant de localhost (peu importe le port).
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));

        // Méthodes HTTP autorisées.
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        // Autorise l’envoi de cookies et credentials.
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        // Applique la configuration sur toutes les routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}


package org.example.projet_final;

import org.example.projet_final.config.FileStorageProperties;
import org.example.projet_final.security.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RsaKeyProperties.class, FileStorageProperties.class})
public class ProjetFinalApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjetFinalApplication.class, args);
    }

}

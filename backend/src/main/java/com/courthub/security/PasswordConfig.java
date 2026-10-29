package com.courthub.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificador de contraseñas (BCrypt, compatible con los hashes de pgcrypto en db/seed.sql).
 * Vive en su propia clase para que los servicios que lo usan (AuthService) no dependan de
 * SecurityConfig, que a su vez depende de ellos a través del manejador de Google.
 */
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}

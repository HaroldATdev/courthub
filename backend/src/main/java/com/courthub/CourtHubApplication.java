package com.courthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CourtHub API — Punto de entrada de la aplicación Spring Boot.
 * Sistema de reservas de canchas deportivas (clientes, entrenadores y administrador/ERP).
 * Se excluye el usuario por defecto de Spring Security: la autenticación es 100 % JWT.
 */
@EnableAsync
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CourtHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourtHubApplication.class, args);
    }
}

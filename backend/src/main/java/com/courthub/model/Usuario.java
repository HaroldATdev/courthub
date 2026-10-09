package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/** Tabla "usuarios" — roles: cliente | administrador | entrenador. */
@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String apellido = "";

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String rol = "cliente";

    @Column(nullable = false, length = 25)
    @Builder.Default
    private String telefono = "";

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String nivel = "Bronce";

    @Column(name = "deporte_favorito", nullable = false, length = 50)
    @Builder.Default
    private String deporteFavorito = "Multi";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = OffsetDateTime.now();
        if (apellido == null) apellido = "";
        if (telefono == null) telefono = "";
        if (nivel == null) nivel = "Bronce";
        if (deporteFavorito == null) deporteFavorito = "Multi";
        if (activo == null) activo = true;
    }

    public String nombreCompleto() {
        return (nombre + " " + (apellido == null ? "" : apellido)).trim();
    }
}

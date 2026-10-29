package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Tabla "usuarios" — perfiles: cliente | administrador | entrenador.
 * Estado de la cuenta: pendiente (por defecto al registrarse) | activo (solo lo otorga el administrador) | anulado.
 */
@Entity
@Table(name = "usuarios")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    public static final String ACTIVO = "activo";
    public static final String PENDIENTE = "pendiente";
    public static final String ANULADO = "anulado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Documento de identidad (8 dígitos). Único: se valida en el registro. */
    @Column(length = 8)
    private String dni;

    /** Nombre de usuario para iniciar sesión. Único: se valida en el registro. */
    @Column(length = 50)
    private String usuario;

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

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = PENDIENTE;

    @Column(name = "fecha_registro", nullable = false)
    private OffsetDateTime fechaRegistro;

    @PrePersist
    void prePersist() {
        if (fechaRegistro == null) fechaRegistro = OffsetDateTime.now();
        if (apellido == null) apellido = "";
        if (telefono == null) telefono = "";
        if (nivel == null) nivel = "Bronce";
        if (deporteFavorito == null) deporteFavorito = "Multi";
        if (estado == null) estado = PENDIENTE;
    }

    public boolean estaActivo() { return ACTIVO.equals(estado); }

    public String nombreCompleto() {
        return (nombre + " " + (apellido == null ? "" : apellido)).trim();
    }
}

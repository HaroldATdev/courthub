package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/** Tabla "claves_reset": solicitudes de restablecimiento de contraseña (se guarda solo el hash del token). */
@Entity
@Table(name = "claves_reset")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ClaveReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /** SHA-256 (hex) del token enviado por correo: si la BD se filtra, los enlaces no sirven. */
    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private OffsetDateTime expira;

    @Column(nullable = false)
    @Builder.Default
    private Boolean usado = false;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
    }
}

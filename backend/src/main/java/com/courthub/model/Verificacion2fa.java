package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/**
 * Tabla "verificaciones_2fa": código de un solo uso enviado al correo tras validar la contraseña.
 * Solo se guardan hashes (del desafío que viaja en el formulario y del código de 6 dígitos).
 */
@Entity
@Table(name = "verificaciones_2fa")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Verificacion2fa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "desafio_hash", nullable = false, unique = true, length = 64)
    private String desafioHash;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(nullable = false)
    private OffsetDateTime expira;

    @Column(nullable = false)
    @Builder.Default
    private Short intentos = 0;

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

package com.courthub.model;

import com.courthub.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

/** Tabla "inscripciones_clases" — estado: inscrito | cancelado | asistio. */
@Entity
@Table(name = "inscripciones_clases", uniqueConstraints = @UniqueConstraint(columnNames = {"clase_id", "usuario_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {

    public static final String INSCRITO = "inscrito";
    public static final String CANCELADO = "cancelado";
    public static final String ASISTIO = "asistio";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clase_id", nullable = false)
    private Clase clase;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = INSCRITO;

    @Column(name = "fecha_inscripcion", nullable = false)
    private OffsetDateTime fechaInscripcion;

    @PrePersist
    void prePersist() {
        if (fechaInscripcion == null) fechaInscripcion = OffsetDateTime.now();
    }
}

package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Tabla "canchas" — fútbol, pádel, tenis, squash. */
@Entity
@Table(name = "canchas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Cancha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(nullable = false, length = 80)
    @Builder.Default
    private String subtipo = "";

    @Column(nullable = false, columnDefinition = "text")
    @Builder.Default
    private String descripcion = "";

    @Column(nullable = false, length = 220)
    @Builder.Default
    private String direccion = "";

    @Column(nullable = false, length = 100)
    private String comuna;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String ciudad = "Lima";

    @Column(name = "precio_hora", nullable = false, precision = 8, scale = 2)
    private BigDecimal precioHora;

    @Column(nullable = false, precision = 2, scale = 1)
    @Builder.Default
    private BigDecimal calificacion = new BigDecimal("4.5");

    @Column(name = "num_opiniones", nullable = false)
    @Builder.Default
    private Integer numOpiniones = 0;

    @Column(name = "imagen_url", nullable = false, length = 400)
    @Builder.Default
    private String imagenUrl = "";

    @Column(nullable = false)
    @Builder.Default
    private Boolean verificada = true;

    /** Arreglo nativo de PostgreSQL (TEXT[]). */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(nullable = false, columnDefinition = "text[]")
    @Builder.Default
    private String[] servicios = new String[0];

    @Column(nullable = false, columnDefinition = "text")
    @Builder.Default
    private String reglamento = "";

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
        if (servicios == null) servicios = new String[0];
    }
}

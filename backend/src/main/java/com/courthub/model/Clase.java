package com.courthub.model;

import com.courthub.model.Cancha;
import com.courthub.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Tabla "clases" — clases deportivas dictadas por entrenadores con cupo limitado. */
@Entity
@Table(name = "clases")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Clase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String deporte;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String nivel = "Todos los niveles";

    @Column(nullable = false, columnDefinition = "text")
    @Builder.Default
    private String descripcion = "";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entrenador_id", nullable = false)
    private Usuario entrenador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancha_id")
    private Cancha cancha;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(name = "cupo_maximo", nullable = false)
    @Builder.Default
    private Integer cupoMaximo = 12;

    @Column(nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal precio = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activa = true;
}

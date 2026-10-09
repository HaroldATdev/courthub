package com.courthub.model;

import com.courthub.model.Cancha;
import com.courthub.model.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

/** Tabla "reservas" — estado: pendiente | confirmada | completada | cancelada. */
@Entity
@Table(name = "reservas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Reserva {

    public static final String PENDIENTE = "pendiente";
    public static final String CONFIRMADA = "confirmada";
    public static final String COMPLETADA = "completada";
    public static final String CANCELADA = "cancelada";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cancha_id", nullable = false)
    private Cancha cancha;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = PENDIENTE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "cargo_servicio", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cargoServicio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "metodo_pago", nullable = false, length = 40)
    @Builder.Default
    private String metodoPago = "Tarjeta de Crédito";

    @Column(nullable = false, columnDefinition = "text")
    @Builder.Default
    private String notas = "";

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) fechaCreacion = OffsetDateTime.now();
        if (notas == null) notas = "";
    }
}

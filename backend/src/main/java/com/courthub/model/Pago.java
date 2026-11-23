package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/** Tabla "pagos" — estado: aprobado | rechazado | reembolsado. Un cobro por reserva. */
@Entity
@Table(name = "pagos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Pago {

    public static final String APROBADO = "aprobado";
    public static final String RECHAZADO = "rechazado";
    public static final String REEMBOLSADO = "reembolsado";

    /** Proveedor mientras no exista una pasarela real (Culqi, Mercado Pago...). */
    public static final String PROVEEDOR_SIMULADO = "simulado";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 30)
    private String operacion;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String proveedor = PROVEEDOR_SIMULADO;

    @Column(nullable = false, length = 40)
    private String metodo;

    /** Solo los últimos 4 dígitos: nunca se guarda el número completo ni el CVV. */
    @Column(name = "tarjeta_ultimos4", length = 4)
    private String tarjetaUltimos4;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "cargo_servicio", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal cargoServicio = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    @Builder.Default
    private String moneda = "PEN";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = APROBADO;

    @Column(name = "fecha_pago", nullable = false)
    private OffsetDateTime fechaPago;

    @PrePersist
    void prePersist() {
        if (fechaPago == null) fechaPago = OffsetDateTime.now();
    }
}

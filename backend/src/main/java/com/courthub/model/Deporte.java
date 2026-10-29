package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

/** Tabla "deportes" — catálogo que alimenta las listas desplegables de canchas y clases. */
@Entity
@Table(name = "deportes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Deporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Valor almacenado en canchas.tipo y clases.deporte (futbol, tenis, squash, padel). */
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    /** Etiqueta visible (Fútbol, Tenis, ...). */
    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Short orden = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}

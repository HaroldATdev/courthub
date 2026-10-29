package com.courthub.model;

import jakarta.persistence.*;
import lombok.*;

/** Tabla "niveles_clase" — catálogo para la lista desplegable de nivel en el CRUD de clases. */
@Entity
@Table(name = "niveles_clase")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class NivelClase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String nombre;

    @Column(nullable = false)
    @Builder.Default
    private Short orden = 0;
}

package com.courthub.dto;
import com.courthub.model.Cancha;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/** Cancha en el formato que consume el frontend (camelCase, reglamento como lista de reglas). */
public record CanchaDto(Long id, String nombre, String tipo, String subtipo, String comuna, String direccion,
                        String ciudad, BigDecimal precioHora, BigDecimal calificacion, Integer opiniones,
                        Boolean verificada, String imagen, List<String> servicios, List<String> reglamento,
                        String descripcion, Boolean activa) {

    public static CanchaDto de(Cancha c) {
        return new CanchaDto(c.getId(), c.getNombre(), c.getTipo(), c.getSubtipo(), c.getComuna(), c.getDireccion(),
                c.getCiudad(), c.getPrecioHora(), c.getCalificacion(), c.getNumOpiniones(), c.getVerificada(),
                c.getImagenUrl(), c.getServicios() == null ? List.of() : Arrays.asList(c.getServicios()),
                reglas(c.getReglamento()), c.getDescripcion(), c.getActiva());
    }

    /** El reglamento se guarda como texto; cada línea (con o sin viñeta "•") es una regla. */
    static List<String> reglas(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        return Arrays.stream(texto.split("\\r?\\n"))
                .map(l -> l.replaceFirst("^\\s*[•\\-\\*]\\s*", "").trim())
                .filter(l -> !l.isEmpty())
                .toList();
    }

    /** Resumen de la cancha dentro de una reserva o clase. */
    public record Snap(Long id, String nombre, String tipo, String comuna, String imagen, String subtipo) {
        public static Snap de(Cancha c) {
            return c == null ? null : new Snap(c.getId(), c.getNombre(), c.getTipo(), c.getComuna(), c.getImagenUrl(), c.getSubtipo());
        }
    }
}

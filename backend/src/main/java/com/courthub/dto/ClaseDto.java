package com.courthub.dto;
import com.courthub.model.Clase;

import com.courthub.util.Horarios;

import java.math.BigDecimal;
import java.util.Map;

/** Clase deportiva en el formato del frontend (cupoMax, inscritos, barra de cupo, entrenador, cancha). */
public record ClaseDto(Long id, String nombre, String deporte, String nivel, String descripcion,
                       String fecha, String horaInicio, String horaFin,
                       Integer cupoMax, Integer inscritos, Integer cuposDisponibles, BigDecimal precio,
                       Persona entrenador, CanchaRef cancha, Boolean inscrito, Boolean activa) {

    public record Persona(Long id, String nombre) {
        /** "Carla Rojas" → "CR" (avatar del entrenador en las vistas). */
        public String iniciales() {
            StringBuilder sb = new StringBuilder();
            for (String p : nombre.trim().split("\\s+")) {
                if (!p.isEmpty() && sb.length() < 2) sb.append(Character.toUpperCase(p.charAt(0)));
            }
            return sb.length() == 0 ? "C" : sb.toString();
        }
    }
    public record CanchaRef(Long id, String nombre, String comuna) {}

    public static ClaseDto de(Clase k, int inscritos, boolean inscrito) {
        return new ClaseDto(k.getId(), k.getNombre(), k.getDeporte(), k.getNivel(), k.getDescripcion(),
                k.getFecha().toString(), Horarios.hora(k.getHoraInicio()), Horarios.hora(k.getHoraFin()),
                k.getCupoMaximo(), inscritos, Math.max(0, k.getCupoMaximo() - inscritos), k.getPrecio(),
                new Persona(k.getEntrenador().getId(), k.getEntrenador().nombreCompleto()),
                k.getCancha() == null ? null : new CanchaRef(k.getCancha().getId(), k.getCancha().getNombre(), k.getCancha().getComuna()),
                inscrito, k.getActiva());
    }

    public static ClaseDto de(Clase k, Map<Long, Integer> inscritosPorClase, boolean inscrito) {
        return de(k, inscritosPorClase.getOrDefault(k.getId(), 0), inscrito);
    }
}

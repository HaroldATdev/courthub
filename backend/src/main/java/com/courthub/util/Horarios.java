package com.courthub.util;

import com.courthub.exception.ApiException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Utilidades de fecha/hora: bloques de 1.5 h y zona horaria de Lima. */
public final class Horarios {

    private Horarios() {}

    public static final ZoneId LIMA = ZoneId.of("America/Lima");
    public static final int DURACION_BLOQUE_MIN = 90;
    public static final DateTimeFormatter HHMM = DateTimeFormatter.ofPattern("HH:mm");

    /** Bloques de inicio disponibles cada día (duración fija: 1.5 h). */
    public static final List<String> BLOQUES = List.of(
            "08:00", "09:30", "11:00", "12:30", "14:00", "15:30", "17:00", "18:30", "20:00", "21:30");

    public static LocalDate hoy() { return LocalDate.now(LIMA); }

    public static LocalTime ahora() { return LocalTime.now(LIMA).withSecond(0).withNano(0); }

    public static String hora(LocalTime t) { return t == null ? null : t.format(HHMM); }

    public static LocalDate parseFecha(String yyyyMMdd, String mensajeError) {
        try {
            return LocalDate.parse(String.valueOf(yyyyMMdd));
        } catch (DateTimeParseException e) {
            throw ApiException.badRequest(mensajeError);
        }
    }

    public static LocalTime parseHora(String hhmm, String mensajeError) {
        try {
            return LocalTime.parse(String.valueOf(hhmm).length() == 5 ? hhmm : String.valueOf(hhmm).substring(0, 5), HHMM);
        } catch (Exception e) {
            throw ApiException.badRequest(mensajeError);
        }
    }

    /** Devuelve TRUE si el bloque (fecha + hora de inicio) ya pasó en hora de Lima. */
    public static boolean yaPaso(LocalDate fecha, LocalTime inicio) {
        LocalDate hoy = hoy();
        return fecha.isBefore(hoy) || (fecha.isEqual(hoy) && !inicio.isAfter(ahora()));
    }
}

package com.courthub.service;

import com.courthub.dto.CanchaDto;
import com.courthub.exception.ApiException;
import com.courthub.repository.CanchaRepository;
import com.courthub.repository.ReservaRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/** Listado con filtros, detalle y disponibilidad por fecha (bloques de 1.5 h). */
@Service
@RequiredArgsConstructor
public class CanchaService {

    private final CanchaRepository canchas;
    private final ReservaRepository reservas;

    /** Un bloque horario del día (vista y API). */
    public record Bloque(String horaInicio, String horaFin, boolean ocupado) {}

    @Transactional(readOnly = true)
    public List<CanchaDto> listar(String tipo, String q, String comuna, String precioMin, String precioMax) {
        String tipoF = vacio(tipo) || "todos".equals(tipo) ? null : tipo;
        String qF = vacio(q) ? null : "%" + q.trim().toLowerCase() + "%";
        String comunaF = vacio(comuna) ? null : comuna.trim();
        return canchas.buscar(tipoF, qF, comunaF, decimal(precioMin), decimal(precioMax))
                .stream().map(CanchaDto::de).toList();
    }

    /** Distritos con canchas activas (para el filtro de ubicación). */
    @Transactional(readOnly = true)
    public List<String> comunas() {
        return canchas.comunasActivas();
    }

    @Transactional(readOnly = true)
    public CanchaDto detalle(Long id) {
        return canchas.findByIdAndActivaTrue(id).map(CanchaDto::de)
                .orElseThrow(() -> ApiException.notFound("Cancha no encontrada."));
    }

    /** Bloques del día: ocupado si hay reserva, si la fecha ya pasó o si el bloque de hoy ya inició. */
    @Transactional(readOnly = true)
    public List<Bloque> bloques(Long canchaId, LocalDate fecha) {
        canchas.findByIdAndActivaTrue(canchaId).orElseThrow(() -> ApiException.notFound("Cancha no encontrada."));
        Set<LocalTime> ocupadas = new HashSet<>(reservas.horasOcupadas(canchaId, fecha));
        List<Bloque> bloques = new ArrayList<>();
        for (String inicioStr : Horarios.BLOQUES) {
            LocalTime inicio = LocalTime.parse(inicioStr);
            LocalTime fin = inicio.plusMinutes(Horarios.DURACION_BLOQUE_MIN);
            boolean ocupado = ocupadas.contains(inicio) || Horarios.yaPaso(fecha, inicio);
            bloques.add(new Bloque(inicioStr, Horarios.hora(fin), ocupado));
        }
        return bloques;
    }

    /** Respuesta de la API: { fecha, bloques[] }. */
    @Transactional(readOnly = true)
    public Map<String, Object> disponibilidad(Long canchaId, String fechaStr) {
        LocalDate fecha = vacio(fechaStr) ? Horarios.hoy() : Horarios.parseFecha(fechaStr, "Fecha inválida (usa yyyy-mm-dd).");
        return Map.of("fecha", fecha.toString(), "bloques", bloques(canchaId, fecha));
    }

    /** ¿El bloque (fecha, hora) está libre y es un bloque válido del día? */
    @Transactional(readOnly = true)
    public boolean bloqueDisponible(Long canchaId, LocalDate fecha, String horaInicio) {
        return bloques(canchaId, fecha).stream().anyMatch(b -> b.horaInicio().equals(horaInicio) && !b.ocupado());
    }

    private static boolean vacio(String s) { return s == null || s.isBlank(); }

    private static BigDecimal decimal(String s) {
        if (vacio(s)) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}

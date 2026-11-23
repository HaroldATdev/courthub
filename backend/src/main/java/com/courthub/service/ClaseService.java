package com.courthub.service;

import com.courthub.dto.ClaseDto;
import com.courthub.exception.ApiException;
import com.courthub.model.Clase;
import com.courthub.model.Inscripcion;
import com.courthub.model.Usuario;
import com.courthub.repository.ClaseRepository;
import com.courthub.repository.InscripcionRepository;
import com.courthub.repository.UsuarioRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Clases deportivas + inscripciones con control de cupo y duplicados. */
@Service
@RequiredArgsConstructor
public class ClaseService {

    private final ClaseRepository clases;
    private final InscripcionRepository inscripciones;
    private final UsuarioRepository usuarios;
    private final PagoService pagos;

    /** Mapa claseId → cantidad de inscritos activos. */
    public Map<Long, Integer> inscritosPorClase() {
        Map<Long, Integer> m = new HashMap<>();
        for (Object[] fila : inscripciones.contarInscritosPorClase()) {
            m.put(((Number) fila[0]).longValue(), ((Number) fila[1]).intValue());
        }
        return m;
    }

    @Transactional(readOnly = true)
    public List<ClaseDto> listar(Long usuarioId, String deporte) {
        String deporteF = (deporte == null || deporte.isBlank() || "todos".equals(deporte)) ? null : deporte;
        Map<Long, Integer> conteo = inscritosPorClase();
        Set<Long> mias = new HashSet<>(inscripciones.clasesInscritas(usuarioId));
        return clases.vigentes(Horarios.hoy(), deporteF).stream()
                .map(k -> ClaseDto.de(k, conteo, mias.contains(k.getId())))
                .toList();
    }

    /** Datos de una clase para la pantalla de pago (con su cupo e indicador de si el usuario ya está inscrito). */
    @Transactional(readOnly = true)
    public ClaseDto paraPago(Long usuarioId, Long claseId) {
        Clase k = clases.findByIdAndActivaTrue(claseId).orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        boolean inscrito = inscripciones.findByClaseIdAndUsuarioId(claseId, usuarioId)
                .map(i -> Inscripcion.INSCRITO.equals(i.getEstado())).orElse(false);
        return ClaseDto.de(k, (int) inscripciones.countByClaseIdAndEstado(claseId, Inscripcion.INSCRITO), inscrito);
    }

    /** Inscripción sin datos de pago (API REST): válida para clases gratuitas y con la pasarela simulada. */
    @Transactional
    public Map<String, Object> inscribirse(Long usuarioId, Long claseId) {
        return inscribirse(usuarioId, claseId, PagoService.DatosPago.metodo("Tarjeta de Crédito"));
    }

    /**
     * Inscribe al usuario y, si la clase tiene precio, la cobra con la pasarela (misma transacción: si el pago
     * es rechazado no queda inscripción ni pago). Las clases gratuitas se inscriben sin cobro.
     */
    @Transactional
    public Map<String, Object> inscribirse(Long usuarioId, Long claseId, PagoService.DatosPago datos) {
        Clase clase = clases.findByIdAndActivaTrue(claseId)
                .orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> ApiException.unauthorized("Usuario no encontrado."));

        if (clase.getFecha().isBefore(Horarios.hoy())) {
            throw ApiException.conflict("Esta clase ya se realizó y no admite inscripciones.");
        }
        var existente = inscripciones.findByClaseIdAndUsuarioId(claseId, usuarioId);
        if (existente.isPresent() && Inscripcion.INSCRITO.equals(existente.get().getEstado())) {
            throw ApiException.conflict("Ya estás inscrito en esta clase.");
        }
        long ocupados = inscripciones.countByClaseIdAndEstado(claseId, Inscripcion.INSCRITO);
        if (ocupados >= clase.getCupoMaximo()) {
            throw ApiException.conflict("Lo sentimos, el cupo está lleno.");
        }
        Inscripcion i;
        try {
            i = existente.orElseGet(() -> Inscripcion.builder().clase(clase).usuario(usuario).build());
            i.setEstado(Inscripcion.INSCRITO);
            i = inscripciones.save(i);
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("Ya estás inscrito en esta clase.");
        }
        boolean cobrada = clase.getPrecio() != null && clase.getPrecio().signum() > 0;
        if (cobrada) pagos.cobrarInscripcion(i, clase, datos);
        return Map.of("ok", true,
                "mensaje", cobrada ? "¡Pago aprobado e inscripción confirmada! Nos vemos en la cancha." : "¡Inscripción confirmada! Nos vemos en la cancha.",
                "claseId", claseId, "inscritos", ocupados + 1);
    }

    /** Anula la inscripción (no se borra: conserva el historial) y reembolsa el pago si lo hubo. */
    @Transactional
    public Map<String, Object> abandonar(Long usuarioId, Long claseId) {
        Inscripcion i = inscripciones.findByClaseIdAndUsuarioId(claseId, usuarioId)
                .filter(x -> Inscripcion.INSCRITO.equals(x.getEstado()))
                .orElseThrow(() -> ApiException.notFound("No estás inscrito en esta clase."));
        i.setEstado(Inscripcion.CANCELADO);
        inscripciones.save(i);
        pagos.reembolsarInscripcion(i.getId());
        return Map.of("ok", true, "mensaje", "Inscripción cancelada.");
    }
}

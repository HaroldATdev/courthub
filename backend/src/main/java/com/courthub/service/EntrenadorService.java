package com.courthub.service;

import com.courthub.exception.ApiException;
import com.courthub.model.Clase;
import com.courthub.model.Inscripcion;
import com.courthub.model.Pago;
import com.courthub.repository.ClaseRepository;
import com.courthub.repository.InscripcionRepository;
import com.courthub.repository.PagoRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ambiente del entrenador: sus clases, los alumnos inscritos, el control de asistencia y lo cobrado por sus clases.
 * Todas las operaciones comprueban que la clase pertenezca al entrenador que las pide.
 */
@Service
@RequiredArgsConstructor
public class EntrenadorService {

    private final ClaseRepository clases;
    private final InscripcionRepository inscripciones;
    private final PagoRepository pagos;

    public record ClaseResumen(Long id, String nombre, String deporte, String nivel, String fecha, String horaInicio, String horaFin,
                               String cancha, int cupoMax, int inscritos, int asistieron, BigDecimal precio, BigDecimal ingresos,
                               boolean pasada, boolean activa) {}

    public record Alumno(Long inscripcionId, String nombre, String email, String telefono, String estado,
                         String fechaInscripcion, BigDecimal pagado, String operacion) {}

    public record Panel(List<ClaseResumen> proximas, List<ClaseResumen> pasadas, long totalClases, long alumnosActivos,
                        long asistencias, BigDecimal ingresos) {}

    public record DetalleClase(ClaseResumen clase, List<Alumno> alumnos) {}

    @Transactional(readOnly = true)
    public Panel panel(Long entrenadorId) {
        Map<Long, Integer> inscritos = inscritosPor(null);
        Map<Long, Integer> asistieron = inscritosPor(Inscripcion.ASISTIO);
        Map<Long, BigDecimal> ingresos = new HashMap<>();
        for (Object[] f : pagos.ingresosPorClase(entrenadorId)) ingresos.put(((Number) f[0]).longValue(), (BigDecimal) f[1]);

        List<ClaseResumen> todas = clases.findByEntrenadorIdOrderByFechaDescHoraInicioDesc(entrenadorId).stream()
                .map(k -> resumen(k, inscritos, asistieron, ingresos)).toList();
        List<ClaseResumen> proximas = todas.stream().filter(c -> !c.pasada()).sorted((a, b) -> (a.fecha() + a.horaInicio()).compareTo(b.fecha() + b.horaInicio())).toList();
        List<ClaseResumen> pasadas = todas.stream().filter(ClaseResumen::pasada).toList();
        long alumnos = proximas.stream().mapToLong(ClaseResumen::inscritos).sum();
        long asistencias = todas.stream().mapToLong(ClaseResumen::asistieron).sum();
        BigDecimal total = todas.stream().map(ClaseResumen::ingresos).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Panel(proximas, pasadas, todas.size(), alumnos, asistencias, total);
    }

    @Transactional(readOnly = true)
    public DetalleClase detalle(Long entrenadorId, Long claseId) {
        Clase k = claseDe(entrenadorId, claseId);
        Map<Long, BigDecimal> ingresos = new HashMap<>();
        for (Object[] f : pagos.ingresosPorClase(entrenadorId)) ingresos.put(((Number) f[0]).longValue(), (BigDecimal) f[1]);
        List<Inscripcion> lista = inscripciones.findByClaseIdOrderByFechaInscripcionAsc(claseId);
        int activos = (int) lista.stream().filter(Inscripcion::estaActiva).count();   // inscrito + asistió
        int asi = (int) lista.stream().filter(i -> Inscripcion.ASISTIO.equals(i.getEstado())).count();
        ClaseResumen r = resumen(k, Map.of(k.getId(), activos), Map.of(k.getId(), asi), ingresos);
        List<Alumno> alumnos = lista.stream()
                .filter(i -> !Inscripcion.CANCELADO.equals(i.getEstado()))
                .map(i -> {
                    Pago p = pagos.findFirstByInscripcionIdAndEstado(i.getId(), Pago.APROBADO).orElse(null);
                    return new Alumno(i.getId(), i.getUsuario().nombreCompleto(), i.getUsuario().getEmail(),
                            i.getUsuario().getTelefono(), i.getEstado(), i.getFechaInscripcion().atZoneSameInstant(Horarios.LIMA).toLocalDate().toString(),
                            p == null ? null : p.getMonto(), p == null ? null : p.getOperacion());
                }).toList();
        return new DetalleClase(r, alumnos);
    }

    /** Marca (o desmarca) la asistencia de un alumno a una clase del entrenador. */
    @Transactional
    public void marcarAsistencia(Long entrenadorId, Long claseId, Long inscripcionId, boolean asistio) {
        claseDe(entrenadorId, claseId);
        Inscripcion i = inscripciones.findById(inscripcionId)
                .filter(x -> x.getClase().getId().equals(claseId))
                .orElseThrow(() -> ApiException.notFound("Inscripción no encontrada en esta clase."));
        if (Inscripcion.CANCELADO.equals(i.getEstado())) {
            throw ApiException.conflict("La inscripción está anulada.");
        }
        i.setEstado(asistio ? Inscripcion.ASISTIO : Inscripcion.INSCRITO);
        inscripciones.save(i);
    }

    private Clase claseDe(Long entrenadorId, Long claseId) {
        return clases.findWithDetalleById(claseId)
                .filter(k -> k.getEntrenador().getId().equals(entrenadorId))
                .orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
    }

    /** Cantidad de personas por clase en el estado dado (null = quienes siguen en la clase: inscrito + asistió). */
    private Map<Long, Integer> inscritosPor(String estado) {
        Map<Long, Integer> m = new HashMap<>();
        for (Inscripcion i : inscripciones.findAll()) {
            boolean cuenta = estado == null ? i.estaActiva() : estado.equals(i.getEstado());
            if (cuenta) m.merge(i.getClase().getId(), 1, Integer::sum);
        }
        return m;
    }

    private static ClaseResumen resumen(Clase k, Map<Long, Integer> inscritos, Map<Long, Integer> asistieron, Map<Long, BigDecimal> ingresos) {
        return new ClaseResumen(k.getId(), k.getNombre(), k.getDeporte(), k.getNivel(), k.getFecha().toString(),
                Horarios.hora(k.getHoraInicio()), Horarios.hora(k.getHoraFin()),
                k.getCancha() == null ? "—" : k.getCancha().getNombre(), k.getCupoMaximo(),
                inscritos.getOrDefault(k.getId(), 0), asistieron.getOrDefault(k.getId(), 0), k.getPrecio(),
                ingresos.getOrDefault(k.getId(), BigDecimal.ZERO), k.getFecha().isBefore(Horarios.hoy()), Boolean.TRUE.equals(k.getActiva()));
    }
}

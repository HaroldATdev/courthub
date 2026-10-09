package com.courthub.service;

import com.courthub.dto.AdminDtos.*;
import com.courthub.model.Cancha;
import com.courthub.dto.CanchaDto;
import com.courthub.repository.CanchaRepository;
import com.courthub.dto.ClaseDto;
import com.courthub.model.Clase;
import com.courthub.model.Inscripcion;
import com.courthub.repository.ClaseRepository;
import com.courthub.repository.InscripcionRepository;
import com.courthub.exception.ApiException;
import com.courthub.util.Horarios;
import com.courthub.model.Reserva;
import com.courthub.dto.ReservaDtos.ReservaDto;
import com.courthub.repository.ReservaRepository;
import com.courthub.security.AuthUser;
import com.courthub.model.Usuario;
import com.courthub.repository.UsuarioRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Lógica del panel ERP del administrador: indicadores (KPIs), reportes agregados y
 * mantenimiento (CRUD) de usuarios, canchas, reservas, clases e inscripciones.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Set<String> ROLES = Set.of(AuthUser.CLIENTE, AuthUser.ENTRENADOR, AuthUser.ADMINISTRADOR);
    private static final Set<String> ESTADOS_RESERVA = Set.of(Reserva.PENDIENTE, Reserva.CONFIRMADA, Reserva.COMPLETADA, Reserva.CANCELADA);
    private static final Set<String> ESTADOS_INSCRIPCION = Set.of(Inscripcion.INSCRITO, Inscripcion.CANCELADO, Inscripcion.ASISTIO);

    private final UsuarioRepository usuarios;
    private final CanchaRepository canchas;
    private final ReservaRepository reservas;
    private final ClaseRepository clases;
    private final InscripcionRepository inscripciones;
    private final ClaseService claseService;
    private final PasswordEncoder encoder;

    /* =====================================================================
       DASHBOARD — indicadores y reportes
       ===================================================================== */

    @Transactional(readOnly = true)
    public Map<String, Object> dashboard() {
        LocalDate hoy = Horarios.hoy();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        LocalDate finMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

        // ---- KPIs ----
        long canchasActivas = canchas.countByActivaTrue();
        long reservasHoy = reservas.countByFechaAndEstadoNot(hoy, Reserva.CANCELADA);
        long capacidadHoy = canchasActivas * Horarios.BLOQUES.size();
        int ocupacionHoy = capacidadHoy == 0 ? 0 : (int) Math.round(reservasHoy * 100.0 / capacidadHoy);

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("ingresosTotales", reservas.ingresosTotales());
        kpis.put("ingresosMes", reservas.ingresosEntre(inicioMes, finMes));
        kpis.put("reservasTotales", reservas.count());
        kpis.put("reservasMes", reservas.countByFechaBetweenAndEstadoNot(inicioMes, finMes, Reserva.CANCELADA));
        kpis.put("reservasHoy", reservasHoy);
        kpis.put("ocupacionHoy", ocupacionHoy);
        kpis.put("usuariosActivos", usuarios.countByActivoTrue());
        kpis.put("canchasActivas", canchasActivas);
        kpis.put("clasesProximas", clases.countByActivaTrueAndFechaGreaterThanEqual(hoy));
        kpis.put("inscripcionesActivas", inscripciones.countByEstado(Inscripcion.INSCRITO));

        // ---- Reservas por estado ----
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (String e : List.of(Reserva.PENDIENTE, Reserva.CONFIRMADA, Reserva.COMPLETADA, Reserva.CANCELADA)) porEstado.put(e, 0L);
        for (Object[] f : reservas.contarPorEstado()) porEstado.put((String) f[0], ((Number) f[1]).longValue());

        // ---- Ingresos por mes (últimos 6 meses, rellenando meses sin ventas) ----
        YearMonth desdeYM = YearMonth.from(hoy).minusMonths(5);
        Map<String, Object[]> porMes = new LinkedHashMap<>();
        for (int i = 0; i < 6; i++) porMes.put(desdeYM.plusMonths(i).toString(), new Object[]{0L, BigDecimal.ZERO});
        for (Object[] f : reservas.ingresosPorMes(desdeYM.atDay(1))) {
            String clave = YearMonth.of(((Number) f[0]).intValue(), ((Number) f[1]).intValue()).toString();
            porMes.put(clave, new Object[]{((Number) f[2]).longValue(), (BigDecimal) f[3]});
        }
        List<Map<String, Object>> ingresosPorMes = new ArrayList<>();
        porMes.forEach((mes, v) -> ingresosPorMes.add(Map.of("mes", mes, "reservas", v[0], "total", v[1])));

        // ---- Ingresos por cancha / deporte / método de pago ----
        List<Map<String, Object>> porCancha = new ArrayList<>();
        for (Object[] f : reservas.ingresosPorCancha()) {
            porCancha.add(Map.of("canchaId", f[0], "cancha", f[1], "tipo", f[2], "reservas", f[3], "total", f[4]));
        }
        List<Map<String, Object>> porDeporte = new ArrayList<>();
        for (Object[] f : reservas.ingresosPorDeporte()) {
            porDeporte.add(Map.of("deporte", f[0], "reservas", f[1], "total", f[2]));
        }
        List<Map<String, Object>> porMetodo = new ArrayList<>();
        for (Object[] f : reservas.ingresosPorMetodoPago()) {
            porMetodo.add(Map.of("metodo", f[0], "reservas", f[1], "total", f[2]));
        }

        // ---- Usuarios por rol ----
        Map<String, Long> porRol = new LinkedHashMap<>();
        for (String r : List.of(AuthUser.CLIENTE, AuthUser.ENTRENADOR, AuthUser.ADMINISTRADOR)) porRol.put(r, 0L);
        for (Object[] f : usuarios.contarPorRol()) porRol.put((String) f[0], ((Number) f[1]).longValue());

        // ---- Últimas reservas y próximas clases ----
        List<ReservaDto> ultimas = reservas.findTop8ByOrderByFechaCreacionDesc().stream().map(r -> ReservaDto.de(r, true)).toList();
        Map<Long, Integer> conteo = claseService.inscritosPorClase();
        List<ClaseDto> proximasClases = clases.vigentes(hoy, null).stream().limit(5).map(k -> ClaseDto.de(k, conteo, false)).toList();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("fecha", hoy.toString());
        out.put("kpis", kpis);
        out.put("reservasPorEstado", porEstado);
        out.put("ingresosPorMes", ingresosPorMes);
        out.put("ingresosPorCancha", porCancha);
        out.put("ingresosPorDeporte", porDeporte);
        out.put("ingresosPorMetodo", porMetodo);
        out.put("usuariosPorRol", porRol);
        out.put("ultimasReservas", ultimas);
        out.put("proximasClases", proximasClases);
        return out;
    }

    /* =====================================================================
       USUARIOS
       ===================================================================== */

    @Transactional(readOnly = true)
    public List<UsuarioAdminDto> usuarios(String q, String rol) {
        String qF = q == null ? "" : q.trim().toLowerCase();
        return usuarios.findAllByOrderByFechaRegistroDesc().stream()
                .filter(u -> rol == null || rol.isBlank() || "todos".equals(rol) || rol.equals(u.getRol()))
                .filter(u -> qF.isEmpty() || (u.nombreCompleto() + " " + u.getEmail()).toLowerCase().contains(qF))
                .map(UsuarioAdminDto::de).toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioAdminDto> entrenadores() {
        return usuarios.findByRolAndActivoTrueOrderByNombreAsc(AuthUser.ENTRENADOR).stream().map(UsuarioAdminDto::de).toList();
    }

    @Transactional
    public UsuarioAdminDto crearUsuario(UsuarioRequest req) {
        if (req.password() == null || req.password().length() < 6) {
            throw ApiException.badRequest("La contraseña debe tener al menos 6 caracteres.");
        }
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) throw ApiException.conflict("Ya existe una cuenta con ese correo.");
        Usuario u = new Usuario();
        aplicar(u, req);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(req.password()));
        return UsuarioAdminDto.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioAdminDto actualizarUsuario(Long id, UsuarioRequest req) {
        Usuario u = buscarUsuario(id);
        String email = req.email().trim().toLowerCase();
        if (!email.equalsIgnoreCase(u.getEmail()) && usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo.");
        }
        aplicar(u, req);
        u.setEmail(email);
        if (req.password() != null && !req.password().isBlank()) {
            if (req.password().length() < 6) throw ApiException.badRequest("La contraseña debe tener al menos 6 caracteres.");
            u.setPasswordHash(encoder.encode(req.password()));
        }
        return UsuarioAdminDto.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioAdminDto estadoUsuario(AuthUser admin, Long id, EstadoRequest req) {
        Usuario u = buscarUsuario(id);
        if (u.getId().equals(admin.id())) throw ApiException.badRequest("No puedes desactivar tu propia cuenta.");
        u.setActivo(req.activo() == null ? !u.getActivo() : req.activo());
        return UsuarioAdminDto.de(usuarios.save(u));
    }

    /** Eliminación lógica: se conserva el historial de reservas del usuario. */
    @Transactional
    public void eliminarUsuario(AuthUser admin, Long id) {
        Usuario u = buscarUsuario(id);
        if (u.getId().equals(admin.id())) throw ApiException.badRequest("No puedes eliminar tu propia cuenta.");
        u.setActivo(false);
        usuarios.save(u);
    }

    private void aplicar(Usuario u, UsuarioRequest req) {
        u.setNombre(req.nombre().trim());
        u.setApellido(req.apellido() == null ? "" : req.apellido().trim());
        u.setRol(ROLES.contains(req.rol()) ? req.rol() : AuthUser.CLIENTE);
        u.setTelefono(req.telefono() == null ? "" : req.telefono().trim());
        u.setNivel(req.nivel() == null || req.nivel().isBlank() ? "Bronce" : req.nivel().trim());
        u.setDeporteFavorito(req.deporteFavorito() == null || req.deporteFavorito().isBlank() ? "Multi" : req.deporteFavorito().trim());
        if (req.activo() != null) u.setActivo(req.activo());
    }

    private Usuario buscarUsuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }

    /* =====================================================================
       CANCHAS
       ===================================================================== */

    @Transactional(readOnly = true)
    public List<CanchaDto> canchas() {
        return canchas.findAllByOrderByNombreAsc().stream().map(CanchaDto::de).toList();
    }

    @Transactional
    public CanchaDto crearCancha(CanchaRequest req) {
        Cancha c = new Cancha();
        aplicar(c, req);
        return CanchaDto.de(canchas.save(c));
    }

    @Transactional
    public CanchaDto actualizarCancha(Long id, CanchaRequest req) {
        Cancha c = buscarCancha(id);
        aplicar(c, req);
        return CanchaDto.de(canchas.save(c));
    }

    @Transactional
    public CanchaDto estadoCancha(Long id, EstadoRequest req) {
        Cancha c = buscarCancha(id);
        c.setActiva(req.activa() == null ? !c.getActiva() : req.activa());
        return CanchaDto.de(canchas.save(c));
    }

    private void aplicar(Cancha c, CanchaRequest req) {
        if (req.precioHora().signum() <= 0) throw ApiException.badRequest("El precio por hora debe ser mayor a 0.");
        c.setNombre(req.nombre().trim());
        c.setTipo(req.tipo().trim().toLowerCase());
        c.setSubtipo(nvl(req.subtipo()));
        c.setDescripcion(nvl(req.descripcion()));
        c.setDireccion(nvl(req.direccion()));
        c.setComuna(req.comuna().trim());
        c.setCiudad(req.ciudad() == null || req.ciudad().isBlank() ? "Lima" : req.ciudad().trim());
        c.setPrecioHora(req.precioHora().setScale(2, RoundingMode.HALF_UP));
        BigDecimal cal = req.calificacion() == null ? new BigDecimal("4.5") : req.calificacion();
        if (cal.compareTo(BigDecimal.ZERO) < 0 || cal.compareTo(new BigDecimal("5")) > 0) {
            throw ApiException.badRequest("La calificación debe estar entre 0 y 5.");
        }
        c.setCalificacion(cal.setScale(1, RoundingMode.HALF_UP));
        c.setNumOpiniones(req.opiniones() == null ? 0 : Math.max(0, req.opiniones()));
        c.setImagenUrl(nvl(req.imagen()));
        c.setVerificada(req.verificada() == null || req.verificada());
        c.setServicios(req.servicios() == null ? new String[0]
                : req.servicios().stream().map(String::trim).filter(s -> !s.isEmpty()).toArray(String[]::new));
        c.setReglamento(req.reglamento() == null ? "" : String.join("\n", req.reglamento()));
        if (req.activa() != null) c.setActiva(req.activa());
    }

    private Cancha buscarCancha(Long id) {
        return canchas.findById(id).orElseThrow(() -> ApiException.notFound("Cancha no encontrada."));
    }

    /* =====================================================================
       RESERVAS
       ===================================================================== */

    @Transactional(readOnly = true)
    public List<ReservaDto> reservas(String estado, String desde, String hasta, Long canchaId, String q) {
        Specification<Reserva> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (estado != null && !estado.isBlank() && !"todos".equals(estado)) p.add(cb.equal(root.get("estado"), estado));
            if (desde != null && !desde.isBlank()) p.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("fecha"), Horarios.parseFecha(desde, "Fecha 'desde' inválida.")));
            if (hasta != null && !hasta.isBlank()) p.add(cb.lessThanOrEqualTo(root.<LocalDate>get("fecha"), Horarios.parseFecha(hasta, "Fecha 'hasta' inválida.")));
            if (canchaId != null) p.add(cb.equal(root.get("cancha").get("id"), canchaId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                Join<Reserva, Usuario> u = root.join("usuario");
                p.add(cb.or(
                        cb.like(cb.lower(root.<String>get("codigo")), like),
                        cb.like(cb.lower(u.<String>get("nombre")), like),
                        cb.like(cb.lower(u.<String>get("apellido")), like),
                        cb.like(cb.lower(u.<String>get("email")), like)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        Sort orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("horaInicio"));
        return reservas.findAll(spec, orden).stream().map(r -> ReservaDto.de(r, true)).toList();
    }

    @Transactional
    public ReservaDto estadoReserva(Long id, EstadoRequest req) {
        if (req.estado() == null || !ESTADOS_RESERVA.contains(req.estado())) {
            throw ApiException.badRequest("Estado inválido. Usa: pendiente, confirmada, completada o cancelada.");
        }
        Reserva r = reservas.findWithDetalleById(id).orElseThrow(() -> ApiException.notFound("Reserva no encontrada."));
        if (!Reserva.CANCELADA.equals(req.estado()) && Reserva.CANCELADA.equals(r.getEstado())) {
            // Reactivar una reserva cancelada: el bloque debe seguir libre
            if (reservas.existeSolapamiento(r.getCancha().getId(), r.getFecha(), r.getHoraInicio(), r.getHoraFin(), r.getId())) {
                throw ApiException.conflict("No se puede reactivar: el bloque ya está ocupado por otra reserva.");
            }
        }
        r.setEstado(req.estado());
        return ReservaDto.de(reservas.save(r), true);
    }

    /* =====================================================================
       CLASES
       ===================================================================== */

    @Transactional(readOnly = true)
    public List<ClaseDto> clases() {
        Map<Long, Integer> conteo = claseService.inscritosPorClase();
        return clases.findAllByOrderByFechaDescHoraInicioDesc().stream().map(k -> ClaseDto.de(k, conteo, false)).toList();
    }

    @Transactional
    public ClaseDto crearClase(ClaseRequest req) {
        Clase k = new Clase();
        aplicar(k, req);
        k = clases.save(k);
        return ClaseDto.de(clases.findWithDetalleById(k.getId()).orElseThrow(), 0, false);
    }

    @Transactional
    public ClaseDto actualizarClase(Long id, ClaseRequest req) {
        Clase k = clases.findWithDetalleById(id).orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        aplicar(k, req);
        clases.save(k);
        int inscritos = (int) inscripciones.countByClaseIdAndEstado(id, Inscripcion.INSCRITO);
        return ClaseDto.de(clases.findWithDetalleById(id).orElseThrow(), inscritos, false);
    }

    @Transactional
    public ClaseDto estadoClase(Long id, EstadoRequest req) {
        Clase k = clases.findWithDetalleById(id).orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        k.setActiva(req.activa() == null ? !k.getActiva() : req.activa());
        clases.save(k);
        int inscritos = (int) inscripciones.countByClaseIdAndEstado(id, Inscripcion.INSCRITO);
        return ClaseDto.de(k, inscritos, false);
    }

    @Transactional(readOnly = true)
    public List<InscripcionDto> inscripcionesDeClase(Long claseId) {
        clases.findById(claseId).orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        return inscripciones.findByClaseIdOrderByFechaInscripcionAsc(claseId).stream().map(InscripcionDto::de).toList();
    }

    private void aplicar(Clase k, ClaseRequest req) {
        Usuario entrenador = usuarios.findById(req.entrenadorId())
                .filter(u -> AuthUser.ENTRENADOR.equals(u.getRol()) || AuthUser.ADMINISTRADOR.equals(u.getRol()))
                .orElseThrow(() -> ApiException.badRequest("El entrenador seleccionado no existe o no tiene rol de entrenador."));
        LocalDate fecha = Horarios.parseFecha(req.fecha(), "Fecha inválida (usa yyyy-mm-dd).");
        LocalTime inicio = Horarios.parseHora(req.horaInicio(), "Hora de inicio inválida (usa HH:mm).");
        LocalTime fin = Horarios.parseHora(req.horaFin(), "Hora de fin inválida (usa HH:mm).");
        if (!fin.isAfter(inicio)) throw ApiException.badRequest("La hora de fin debe ser posterior a la de inicio.");
        int cupo = req.cupoMaximo() == null ? 12 : req.cupoMaximo();
        if (cupo <= 0) throw ApiException.badRequest("El cupo máximo debe ser mayor a 0.");
        if (k.getId() != null) {
            long inscritos = inscripciones.countByClaseIdAndEstado(k.getId(), Inscripcion.INSCRITO);
            if (cupo < inscritos) throw ApiException.badRequest("El cupo no puede ser menor que los inscritos actuales (" + inscritos + ").");
        }
        k.setNombre(req.nombre().trim());
        k.setDeporte(req.deporte().trim().toLowerCase());
        k.setNivel(req.nivel() == null || req.nivel().isBlank() ? "Todos los niveles" : req.nivel().trim());
        k.setDescripcion(nvl(req.descripcion()));
        k.setEntrenador(entrenador);
        k.setCancha(req.canchaId() == null ? null : buscarCancha(req.canchaId()));
        k.setFecha(fecha);
        k.setHoraInicio(inicio);
        k.setHoraFin(fin);
        k.setCupoMaximo(cupo);
        k.setPrecio(req.precio() == null ? BigDecimal.ZERO : req.precio().setScale(2, RoundingMode.HALF_UP));
        if (req.activa() != null) k.setActiva(req.activa());
    }

    /* =====================================================================
       INSCRIPCIONES
       ===================================================================== */

    @Transactional(readOnly = true)
    public List<InscripcionDto> inscripciones(String estado) {
        return inscripciones.findAllByOrderByFechaInscripcionDesc().stream()
                .filter(i -> estado == null || estado.isBlank() || "todos".equals(estado) || estado.equals(i.getEstado()))
                .map(InscripcionDto::de).toList();
    }

    @Transactional
    public InscripcionDto estadoInscripcion(Long id, EstadoRequest req) {
        if (req.estado() == null || !ESTADOS_INSCRIPCION.contains(req.estado())) {
            throw ApiException.badRequest("Estado inválido. Usa: inscrito, cancelado o asistio.");
        }
        Inscripcion i = inscripciones.findById(id).orElseThrow(() -> ApiException.notFound("Inscripción no encontrada."));
        if (Inscripcion.INSCRITO.equals(req.estado()) && !Inscripcion.INSCRITO.equals(i.getEstado())) {
            long ocupados = inscripciones.countByClaseIdAndEstado(i.getClase().getId(), Inscripcion.INSCRITO);
            if (ocupados >= i.getClase().getCupoMaximo()) throw ApiException.conflict("El cupo de la clase está lleno.");
        }
        i.setEstado(req.estado());
        inscripciones.save(i);
        return InscripcionDto.de(i);
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }
}

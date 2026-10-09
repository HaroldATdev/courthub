package com.courthub.service;

import com.courthub.dto.ReservaDtos.*;
import com.courthub.exception.ApiException;
import com.courthub.model.Cancha;
import com.courthub.model.Reserva;
import com.courthub.model.Usuario;
import com.courthub.repository.CanchaRepository;
import com.courthub.repository.ReservaRepository;
import com.courthub.repository.UsuarioRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

/** Crear, listar, cancelar y reprogramar reservas (bloques de 1.5 h, cargo por servicio 10 %). */
@Service
@RequiredArgsConstructor
public class ReservaService {

    public static final BigDecimal HORAS_BLOQUE = new BigDecimal("1.5");
    public static final BigDecimal TASA_CARGO = new BigDecimal("0.10");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ReservaRepository reservas;
    private final CanchaRepository canchas;
    private final UsuarioRepository usuarios;

    /** Importes de un bloque: subtotal (precio/h × 1.5), cargo por servicio (10 %) y total. */
    public record Cotizacion(BigDecimal subtotal, BigDecimal cargo, BigDecimal total) {
        public static Cotizacion de(BigDecimal precioHora) {
            BigDecimal subtotal = precioHora.multiply(HORAS_BLOQUE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal cargo = subtotal.multiply(TASA_CARGO).setScale(2, RoundingMode.HALF_UP);
            return new Cotizacion(subtotal, cargo, subtotal.add(cargo));
        }
    }

    /** Adaptador para la API REST (body en snake_case). */
    @Transactional
    public ReservaDto crear(Long usuarioId, CrearRequest req) {
        if (req.canchaId() == null || req.fecha() == null || req.horaInicio() == null) {
            throw ApiException.badRequest("Datos de reserva incompletos o inválidos.");
        }
        LocalDate fecha = Horarios.parseFecha(req.fecha(), "Datos de reserva incompletos o inválidos.");
        LocalTime inicio = Horarios.parseHora(req.horaInicio(), "Datos de reserva incompletos o inválidos.");
        return crear(usuarioId, req.canchaId(), fecha, inicio, req.metodoPago());
    }

    @Transactional
    public ReservaDto crear(Long usuarioId, Long canchaId, LocalDate fecha, LocalTime inicio, String metodoPago) {
        if (Horarios.yaPaso(fecha, inicio)) {
            throw ApiException.badRequest("No puedes reservar bloques que ya pasaron.");
        }
        Cancha cancha = canchas.findByIdAndActivaTrue(canchaId)
                .orElseThrow(() -> ApiException.notFound("Cancha no encontrada."));
        Usuario usuario = usuarios.findById(usuarioId)
                .orElseThrow(() -> ApiException.unauthorized("Usuario no encontrado."));

        LocalTime fin = inicio.plusMinutes(Horarios.DURACION_BLOQUE_MIN);
        if (reservas.existeSolapamiento(cancha.getId(), fecha, inicio, fin, null)) {
            throw ApiException.conflict("Ese bloque horario ya no está disponible. Elige otro.");
        }
        Cotizacion c = Cotizacion.de(cancha.getPrecioHora());
        Reserva r = Reserva.builder()
                .codigo(generarCodigo())
                .usuario(usuario).cancha(cancha)
                .fecha(fecha).horaInicio(inicio).horaFin(fin)
                .estado(Reserva.CONFIRMADA)
                .subtotal(c.subtotal()).cargoServicio(c.cargo()).total(c.total())
                .metodoPago(metodoPago == null || metodoPago.isBlank() ? "Tarjeta de Crédito" : metodoPago)
                .build();
        return ReservaDto.de(reservas.save(r));
    }

    /** Reservas del usuario (más recientes primero). */
    @Transactional(readOnly = true)
    public List<ReservaDto> listar(Long usuarioId) {
        return reservas.findByUsuarioIdOrderByFechaDescHoraInicioDesc(usuarioId).stream().map(ReservaDto::de).toList();
    }

    /** Todas las reservas de la red (administrador). */
    @Transactional(readOnly = true)
    public List<ReservaDto> listarTodas() {
        Sort orden = Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("horaInicio"));
        return reservas.findAll((Specification<Reserva>) null, orden).stream().map(r -> ReservaDto.de(r, true)).toList();
    }

    @Transactional
    public ReservaDto cancelar(Long usuarioId, Long id) {
        Reserva r = reservas.findByIdAndUsuarioId(id, usuarioId)
                .filter(x -> Set.of(Reserva.PENDIENTE, Reserva.CONFIRMADA).contains(x.getEstado()))
                .orElseThrow(() -> ApiException.notFound("Reserva no encontrada o ya cancelada."));
        r.setEstado(Reserva.CANCELADA);
        return ReservaDto.de(reservas.save(r));
    }

    @Transactional
    public ReservaDto reprogramar(Long usuarioId, Long id, ReprogramarRequest req) {
        if (req.fecha() == null || req.horaInicio() == null) {
            throw ApiException.badRequest("Indica la nueva fecha y hora de la reserva.");
        }
        LocalDate fecha = Horarios.parseFecha(req.fecha(), "Indica la nueva fecha y hora de la reserva.");
        LocalTime inicio = Horarios.parseHora(req.horaInicio(), "Indica la nueva fecha y hora de la reserva.");
        return reprogramar(usuarioId, id, fecha, inicio);
    }

    @Transactional
    public ReservaDto reprogramar(Long usuarioId, Long id, LocalDate fecha, LocalTime inicio) {
        if (fecha.isBefore(Horarios.hoy())) {
            throw ApiException.badRequest("La nueva fecha no puede ser pasada.");
        }
        Reserva r = reservas.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> ApiException.notFound("Reserva no encontrada."));
        LocalTime fin = inicio.plusMinutes(Horarios.DURACION_BLOQUE_MIN);
        if (reservas.existeSolapamiento(r.getCancha().getId(), fecha, inicio, fin, r.getId())) {
            throw ApiException.conflict("El nuevo bloque ya está ocupado. Elige otro horario.");
        }
        r.setFecha(fecha);
        r.setHoraInicio(inicio);
        r.setHoraFin(fin);
        r.setEstado(Reserva.CONFIRMADA);
        return ReservaDto.de(reservas.save(r));
    }

    /** Código legible tipo CH-84512 (único). */
    public String generarCodigo() {
        String codigo;
        do {
            codigo = "CH-" + (10000 + RANDOM.nextInt(90000));
        } while (reservas.existsByCodigo(codigo));
        return codigo;
    }
}

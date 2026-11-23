package com.courthub.service;

import com.courthub.exception.ApiException;
import com.courthub.model.Clase;
import com.courthub.model.Inscripcion;
import com.courthub.model.Pago;
import com.courthub.model.Reserva;
import com.courthub.pasarela.PasarelaPago;
import com.courthub.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Optional;

/**
 * Cobros de reservas e inscripciones a clases. El cobro lo hace la pasarela configurada (simulada o Culqi sandbox)
 * y cada pago aprobado queda registrado en "pagos". Todo ocurre dentro de la transacción de la reserva o
 * inscripción: si la pasarela rechaza el pago, se lanza una excepción y no queda ni la reserva ni el pago.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PagoRepository pagos;
    private final PasarelaPago pasarela;

    /** Datos de pago que llegan del formulario: método, token de la pasarela (Culqi) o número de tarjeta (simulada). */
    public record DatosPago(String metodo, String token, String numeroTarjeta) {
        public static DatosPago metodo(String metodo) { return new DatosPago(metodo, null, null); }
    }

    public PasarelaPago pasarela() { return pasarela; }

    /** Cobra una reserva ya guardada. Lanza 402 si la pasarela rechaza el pago (la transacción se deshace). */
    @Transactional
    public Pago cobrarReserva(Reserva reserva, DatosPago datos) {
        PasarelaPago.Resultado r = cobrar(reserva.getTotal(), "CourtHub reserva " + reserva.getCodigo(),
                reserva.getUsuario().getEmail(), datos);
        return pagos.save(Pago.builder()
                .reserva(reserva).usuario(reserva.getUsuario())
                .operacion(generarOperacion()).proveedor(pasarela.nombre()).referencia(r.referencia())
                .metodo(reserva.getMetodoPago()).tarjetaUltimos4(r.ultimos4())
                .subtotal(reserva.getSubtotal()).cargoServicio(reserva.getCargoServicio()).monto(reserva.getTotal())
                .build());
    }

    /** Cobra la inscripción a una clase de pago. Las clases gratuitas no pasan por aquí. */
    @Transactional
    public Pago cobrarInscripcion(Inscripcion inscripcion, Clase clase, DatosPago datos) {
        PasarelaPago.Resultado r = cobrar(clase.getPrecio(), "CourtHub clase " + clase.getNombre(),
                inscripcion.getUsuario().getEmail(), datos);
        return pagos.save(Pago.builder()
                .inscripcion(inscripcion).usuario(inscripcion.getUsuario())
                .operacion(generarOperacion()).proveedor(pasarela.nombre()).referencia(r.referencia())
                .metodo(metodoDe(datos)).tarjetaUltimos4(r.ultimos4())
                .subtotal(clase.getPrecio()).cargoServicio(BigDecimal.ZERO).monto(clase.getPrecio())
                .build());
    }

    private PasarelaPago.Resultado cobrar(BigDecimal monto, String descripcion, String email, DatosPago datos) {
        PasarelaPago.Resultado r = pasarela.cobrar(new PasarelaPago.Cobro(
                monto, descripcion, email, metodoDe(datos), datos == null ? null : datos.token(),
                datos == null ? null : datos.numeroTarjeta()));
        if (!r.aprobado()) {
            throw new ApiException(HttpStatus.PAYMENT_REQUIRED, "Pago rechazado: " + r.mensaje());
        }
        return r;
    }

    private static String metodoDe(DatosPago d) {
        return d == null || d.metodo() == null || d.metodo().isBlank() ? "Tarjeta de Crédito" : d.metodo();
    }

    /* ---------------- Reembolsos y cobros manuales ---------------- */

    /** Si la reserva se cancela, su cobro aprobado pasa a "reembolsado" (no cuenta como venta). */
    @Transactional
    public void reembolsar(Long reservaId) {
        pagos.findByReservaIdAndEstado(reservaId, Pago.APROBADO).ifPresent(this::marcarReembolsado);
    }

    /** Igual que {@link #reembolsar} para una inscripción que se anula. */
    @Transactional
    public void reembolsarInscripcion(Long inscripcionId) {
        pagos.findFirstByInscripcionIdAndEstado(inscripcionId, Pago.APROBADO).ifPresent(this::marcarReembolsado);
    }

    private void marcarReembolsado(Pago p) {
        p.setEstado(Pago.REEMBOLSADO);
        pagos.save(p);
    }

    /**
     * Cuando el admin confirma o completa una reserva, debe existir su cobro aprobado: si estaba reembolsado
     * (reserva reactivada) vuelve a aprobado; si nunca se cobró (reserva pendiente, p. ej. pago en el local)
     * se registra un cobro manual con el método de la reserva.
     */
    @Transactional
    public void asegurarCobro(Reserva reserva) {
        if (pagos.findByReservaIdAndEstado(reserva.getId(), Pago.APROBADO).isPresent()) return;
        Optional<Pago> reembolsado = pagos.findByReservaIdAndEstado(reserva.getId(), Pago.REEMBOLSADO);
        if (reembolsado.isPresent()) {
            reembolsado.get().setEstado(Pago.APROBADO);
            pagos.save(reembolsado.get());
        } else {
            pagos.save(Pago.builder()
                    .reserva(reserva).usuario(reserva.getUsuario())
                    .operacion(generarOperacion()).proveedor(Pago.PROVEEDOR_MANUAL)
                    .metodo(reserva.getMetodoPago())
                    .subtotal(reserva.getSubtotal()).cargoServicio(reserva.getCargoServicio()).monto(reserva.getTotal())
                    .build());
        }
    }

    /** Reactivar una inscripción anulada: su último cobro reembolsado vuelve a ser válido (si lo había). */
    @Transactional
    public void reactivarInscripcion(Inscripcion inscripcion) {
        if (pagos.findFirstByInscripcionIdAndEstado(inscripcion.getId(), Pago.APROBADO).isPresent()) return;
        pagos.findFirstByInscripcionIdAndEstado(inscripcion.getId(), Pago.REEMBOLSADO).ifPresent(p -> {
            p.setEstado(Pago.APROBADO);
            pagos.save(p);
        });
    }

    /** Número de operación único, p. ej. OP-48210377. */
    private String generarOperacion() {
        String op;
        do {
            op = "OP-" + (10_000_000 + RANDOM.nextInt(90_000_000));
        } while (pagos.existsByOperacion(op));
        return op;
    }
}

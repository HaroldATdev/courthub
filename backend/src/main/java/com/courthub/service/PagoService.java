package com.courthub.service;

import com.courthub.model.Pago;
import com.courthub.model.Reserva;
import com.courthub.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Registro de cobros. Hoy el proveedor es "simulado" (no sale dinero a ningún lado), pero cada reserva
 * confirmada deja su pago registrado; al integrar una pasarela solo cambia quién aprueba el cobro.
 * Se ejecuta dentro de la transacción de la reserva: si falla uno, no queda el otro.
 */
@Service
@RequiredArgsConstructor
public class PagoService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final PagoRepository pagos;

    /** Registra el cobro aprobado de una reserva recién creada. */
    @Transactional
    public Pago registrarAprobado(Reserva reserva, String metodo, String tarjetaUltimos4) {
        return pagos.save(Pago.builder()
                .reserva(reserva).usuario(reserva.getUsuario())
                .operacion(generarOperacion())
                .metodo(metodo)
                .tarjetaUltimos4(tarjetaUltimos4)
                .subtotal(reserva.getSubtotal()).cargoServicio(reserva.getCargoServicio()).monto(reserva.getTotal())
                .build());
    }

    /** Si la reserva se cancela, su cobro aprobado pasa a "reembolsado" (no cuenta como venta). */
    @Transactional
    public void reembolsar(Long reservaId) {
        pagos.findByReservaIdAndEstado(reservaId, Pago.APROBADO).ifPresent(p -> {
            p.setEstado(Pago.REEMBOLSADO);
            pagos.save(p);
        });
    }

    /**
     * Cuando el admin confirma o completa una reserva, debe existir su cobro aprobado: si estaba reembolsado
     * (reserva reactivada) vuelve a aprobado; si nunca se cobró (reserva pendiente, p. ej. pago en el local)
     * se registra el cobro con el método de la reserva.
     */
    @Transactional
    public void asegurarCobro(Reserva reserva) {
        if (pagos.findByReservaIdAndEstado(reserva.getId(), Pago.APROBADO).isPresent()) return;
        var reembolsado = pagos.findByReservaIdAndEstado(reserva.getId(), Pago.REEMBOLSADO);
        if (reembolsado.isPresent()) {
            reembolsado.get().setEstado(Pago.APROBADO);
            pagos.save(reembolsado.get());
        } else {
            registrarAprobado(reserva, reserva.getMetodoPago(), null);
        }
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

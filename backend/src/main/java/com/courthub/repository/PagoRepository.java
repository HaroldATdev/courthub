package com.courthub.repository;

import com.courthub.model.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByOperacion(String operacion);

    /** Cobro vigente de una reserva (aprobado). */
    Optional<Pago> findByReservaIdAndEstado(Long reservaId, String estado);

    /** Pago de una reserva con todo lo necesario para emitir la boleta. */
    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario"})
    Optional<Pago> findFirstByReservaIdAndEstadoInOrderByIdDesc(Long reservaId, Collection<String> estados);

    /** Pagos con su reserva, cancha y cliente (reporte de ventas), del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario"})
    List<Pago> findAllByOrderByFechaPagoDesc();
}

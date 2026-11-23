package com.courthub.repository;

import com.courthub.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByOperacion(String operacion);

    /** Cobro vigente de una reserva (aprobado). */
    Optional<Pago> findByReservaIdAndEstado(Long reservaId, String estado);
}

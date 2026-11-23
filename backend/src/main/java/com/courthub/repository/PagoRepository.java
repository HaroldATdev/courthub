package com.courthub.repository;

import com.courthub.model.Pago;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    boolean existsByOperacion(String operacion);

    /** Cobro vigente de una reserva (aprobado). */
    Optional<Pago> findByReservaIdAndEstado(Long reservaId, String estado);

    /** Cobro vigente de una inscripción (aprobado). */
    Optional<Pago> findFirstByInscripcionIdAndEstado(Long inscripcionId, String estado);

    /** Pago de una reserva con todo lo necesario para emitir la boleta. */
    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario", "inscripcion", "inscripcion.clase"})
    Optional<Pago> findFirstByReservaIdAndEstadoInOrderByIdDesc(Long reservaId, Collection<String> estados);

    /** Pago de una inscripción con todo lo necesario para emitir la boleta. */
    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario", "inscripcion", "inscripcion.clase"})
    Optional<Pago> findFirstByInscripcionIdAndEstadoInOrderByIdDesc(Long inscripcionId, Collection<String> estados);

    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario", "inscripcion", "inscripcion.clase"})
    Optional<Pago> findWithOrigenById(Long id);

    /** Ingresos cobrados por clase de un entrenador: [claseId, suma de pagos aprobados]. */
    @Query("SELECT p.inscripcion.clase.id, SUM(p.monto) FROM Pago p " +
           "WHERE p.estado = 'aprobado' AND p.inscripcion.clase.entrenador.id = :entrenadorId GROUP BY p.inscripcion.clase.id")
    List<Object[]> ingresosPorClase(@Param("entrenadorId") Long entrenadorId);

    /** Pagos con su origen (reserva o clase) y cliente (reporte de ventas), del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"reserva", "reserva.cancha", "usuario", "inscripcion", "inscripcion.clase"})
    List<Pago> findAllByOrderByFechaPagoDesc();
}

package com.courthub.repository;
import com.courthub.model.Reserva;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {

    boolean existsByCodigo(String codigo);

    @EntityGraph(attributePaths = {"cancha"})
    List<Reserva> findByUsuarioIdOrderByFechaDescHoraInicioDesc(Long usuarioId);

    @EntityGraph(attributePaths = {"cancha", "usuario"})
    Optional<Reserva> findWithDetalleById(Long id);

    Optional<Reserva> findByIdAndUsuarioId(Long id, Long usuarioId);

    /** Listado admin con filtros dinámicos (Specification) y joins precargados. */
    @EntityGraph(attributePaths = {"cancha", "usuario"})
    List<Reserva> findAll(Specification<Reserva> spec, Sort sort);

    /** Horas de inicio ocupadas en una cancha y fecha (para pintar los bloques). */
    @Query("SELECT r.horaInicio FROM Reserva r WHERE r.cancha.id = :canchaId AND r.fecha = :fecha AND r.estado <> 'cancelada'")
    List<LocalTime> horasOcupadas(@Param("canchaId") Long canchaId, @Param("fecha") LocalDate fecha);

    /** ¿Existe alguna reserva activa que se solape con [inicio, fin) ? (excluyendo opcionalmente una reserva). */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reserva r
            WHERE r.cancha.id = :canchaId AND r.fecha = :fecha AND r.estado <> 'cancelada'
              AND r.horaInicio < :fin AND :inicio < r.horaFin
              AND (:excluirId IS NULL OR r.id <> :excluirId)
            """)
    boolean existeSolapamiento(@Param("canchaId") Long canchaId, @Param("fecha") LocalDate fecha,
                               @Param("inicio") LocalTime inicio, @Param("fin") LocalTime fin,
                               @Param("excluirId") Long excluirId);

    /* ---------------- Consultas agregadas para el panel ERP ---------------- */

    @Query("SELECT r.estado, COUNT(r) FROM Reserva r GROUP BY r.estado")
    List<Object[]> contarPorEstado();

    @Query("SELECT COALESCE(SUM(r.total), 0) FROM Reserva r WHERE r.estado <> 'cancelada'")
    BigDecimal ingresosTotales();

    @Query("SELECT COALESCE(SUM(r.total), 0) FROM Reserva r WHERE r.estado <> 'cancelada' AND r.fecha BETWEEN :desde AND :hasta")
    BigDecimal ingresosEntre(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    long countByFechaBetweenAndEstadoNot(LocalDate desde, LocalDate hasta, String estado);

    long countByFechaAndEstadoNot(LocalDate fecha, String estado);

    @Query("""
            SELECT YEAR(r.fecha), MONTH(r.fecha), COUNT(r), COALESCE(SUM(r.total), 0)
            FROM Reserva r
            WHERE r.estado <> 'cancelada' AND r.fecha >= :desde
            GROUP BY YEAR(r.fecha), MONTH(r.fecha)
            ORDER BY YEAR(r.fecha), MONTH(r.fecha)
            """)
    List<Object[]> ingresosPorMes(@Param("desde") LocalDate desde);

    @Query("""
            SELECT c.id, c.nombre, c.tipo, COUNT(r), COALESCE(SUM(r.total), 0)
            FROM Reserva r JOIN r.cancha c
            WHERE r.estado <> 'cancelada'
            GROUP BY c.id, c.nombre, c.tipo
            ORDER BY SUM(r.total) DESC
            """)
    List<Object[]> ingresosPorCancha();

    @Query("""
            SELECT c.tipo, COUNT(r), COALESCE(SUM(r.total), 0)
            FROM Reserva r JOIN r.cancha c
            WHERE r.estado <> 'cancelada'
            GROUP BY c.tipo ORDER BY SUM(r.total) DESC
            """)
    List<Object[]> ingresosPorDeporte();

    @Query("""
            SELECT r.metodoPago, COUNT(r), COALESCE(SUM(r.total), 0)
            FROM Reserva r WHERE r.estado <> 'cancelada'
            GROUP BY r.metodoPago ORDER BY SUM(r.total) DESC
            """)
    List<Object[]> ingresosPorMetodoPago();

    @EntityGraph(attributePaths = {"cancha", "usuario"})
    List<Reserva> findTop8ByOrderByFechaCreacionDesc();
}

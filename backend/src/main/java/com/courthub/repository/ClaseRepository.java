package com.courthub.repository;
import com.courthub.model.Clase;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClaseRepository extends JpaRepository<Clase, Long> {

    Optional<Clase> findByIdAndActivaTrue(Long id);

    @EntityGraph(attributePaths = {"entrenador", "cancha"})
    Optional<Clase> findWithDetalleById(Long id);

    /** Clases vigentes (desde hoy) con filtro opcional por deporte. */
    @EntityGraph(attributePaths = {"entrenador", "cancha"})
    @Query("""
            SELECT k FROM Clase k
            WHERE k.activa = TRUE AND k.fecha >= :desde
              AND (:deporte IS NULL OR k.deporte = :deporte)
            ORDER BY k.fecha ASC, k.horaInicio ASC
            """)
    List<Clase> vigentes(@Param("desde") LocalDate desde, @Param("deporte") String deporte);

    /** Todas las clases (panel admin). */
    @EntityGraph(attributePaths = {"entrenador", "cancha"})
    List<Clase> findAllByOrderByFechaDescHoraInicioDesc();

    long countByActivaTrueAndFechaGreaterThanEqual(LocalDate fecha);
}

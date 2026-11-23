package com.courthub.repository;
import com.courthub.model.Inscripcion;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long> {

    Optional<Inscripcion> findByClaseIdAndUsuarioId(Long claseId, Long usuarioId);

    long countByClaseIdAndEstado(Long claseId, String estado);

    long countByClaseIdAndEstadoIn(Long claseId, java.util.Collection<String> estados);

    long countByEstado(String estado);

    /** Ids de clases en las que el usuario está inscrito. */
    @Query("SELECT i.clase.id FROM Inscripcion i WHERE i.usuario.id = :usuarioId AND i.estado IN ('inscrito', 'asistio')")
    List<Long> clasesInscritas(@Param("usuarioId") Long usuarioId);

    /** Inscritos activos por clase: [claseId, cantidad]. */
    @Query("SELECT i.clase.id, COUNT(i) FROM Inscripcion i WHERE i.estado IN ('inscrito', 'asistio') GROUP BY i.clase.id")
    List<Object[]> contarInscritosPorClase();

    @EntityGraph(attributePaths = {"usuario", "clase"})
    List<Inscripcion> findByClaseIdOrderByFechaInscripcionAsc(Long claseId);

    @EntityGraph(attributePaths = {"usuario", "clase", "clase.entrenador"})
    List<Inscripcion> findAllByOrderByFechaInscripcionDesc();
}

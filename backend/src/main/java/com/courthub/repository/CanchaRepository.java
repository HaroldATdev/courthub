package com.courthub.repository;

import com.courthub.model.Cancha;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CanchaRepository extends JpaRepository<Cancha, Long> {

    Optional<Cancha> findByIdAndActivaTrue(Long id);

    List<Cancha> findAllByOrderByNombreAsc();

    List<Cancha> findByActivaTrueOrderByNombreAsc();

    long countByActivaTrue();

    /** Listado público con filtros opcionales (tipo, texto, distrito, rango de precio). */
    @Query("""
            SELECT c FROM Cancha c
            WHERE c.activa = TRUE
              AND (:tipo IS NULL OR c.tipo = :tipo)
              AND (:q IS NULL OR LOWER(c.nombre) LIKE :q OR LOWER(c.comuna) LIKE :q OR LOWER(c.tipo) LIKE :q)
              AND (:comuna IS NULL OR c.comuna = :comuna)
              AND (:precioMin IS NULL OR c.precioHora >= :precioMin)
              AND (:precioMax IS NULL OR c.precioHora <= :precioMax)
            ORDER BY c.calificacion DESC, c.nombre ASC
            """)
    List<Cancha> buscar(@Param("tipo") String tipo,
                        @Param("q") String q,
                        @Param("comuna") String comuna,
                        @Param("precioMin") BigDecimal precioMin,
                        @Param("precioMax") BigDecimal precioMax);

    @Query("SELECT DISTINCT c.comuna FROM Cancha c WHERE c.activa = TRUE ORDER BY c.comuna")
    List<String> comunasActivas();
}

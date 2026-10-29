package com.courthub.repository;

import com.courthub.model.Deporte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeporteRepository extends JpaRepository<Deporte, Long> {

    List<Deporte> findByActivoTrueOrderByOrdenAscNombreAsc();

    Optional<Deporte> findByCodigo(String codigo);

    boolean existsByCodigoAndActivoTrue(String codigo);
}

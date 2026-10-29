package com.courthub.repository;

import com.courthub.model.NivelClase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NivelClaseRepository extends JpaRepository<NivelClase, Long> {

    List<NivelClase> findAllByOrderByOrdenAscNombreAsc();

    boolean existsByNombre(String nombre);
}

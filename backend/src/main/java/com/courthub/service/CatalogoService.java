package com.courthub.service;

import com.courthub.model.Deporte;
import com.courthub.model.NivelClase;
import com.courthub.repository.DeporteRepository;
import com.courthub.repository.NivelClaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Catálogos de referencia (tablas "deportes" y "niveles_clase") que alimentan las listas
 * desplegables de los CRUD de canchas y clases y los filtros del dashboard (tercer avance, punto 5).
 */
@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final DeporteRepository deportes;
    private final NivelClaseRepository niveles;

    @Transactional(readOnly = true)
    public List<Deporte> deportes() {
        return deportes.findByActivoTrueOrderByOrdenAscNombreAsc();
    }

    /** Mapa código → nombre, con la opción "todos" al inicio (filtros del dashboard y de clases). */
    @Transactional(readOnly = true)
    public Map<String, String> mapaDeportes() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("todos", "Todos");
        for (Deporte d : deportes()) m.put(d.getCodigo(), d.getNombre());
        return m;
    }

    @Transactional(readOnly = true)
    public boolean existeDeporte(String codigo) {
        return codigo != null && deportes.existsByCodigoAndActivoTrue(codigo);
    }

    @Transactional(readOnly = true)
    public String nombreDeporte(String codigo) {
        if (codigo == null || codigo.isEmpty()) return "";
        return deportes.findByCodigo(codigo).map(Deporte::getNombre)
                .orElse(codigo.substring(0, 1).toUpperCase() + codigo.substring(1));
    }

    @Transactional(readOnly = true)
    public List<NivelClase> niveles() {
        return niveles.findAllByOrderByOrdenAscNombreAsc();
    }

    @Transactional(readOnly = true)
    public boolean existeNivel(String nombre) {
        return nombre != null && niveles.existsByNombre(nombre);
    }
}

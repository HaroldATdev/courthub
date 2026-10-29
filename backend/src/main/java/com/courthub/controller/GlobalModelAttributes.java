package com.courthub.controller;

import com.courthub.model.Deporte;
import com.courthub.model.NivelClase;
import com.courthub.security.AuthUser;
import com.courthub.service.CatalogoService;
import com.courthub.util.Fmt;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

/**
 * Atributos disponibles en TODAS las vistas Thymeleaf (header, filtros, fechas y catálogos):
 * ${usuario}, ${esAdmin}, ${deportes} (mapa código→nombre desde la tabla deportes),
 * ${deportesDb} y ${niveles} (listas desplegables cargadas desde la BD), ${bloques}, ${hoy}.
 */
@ControllerAdvice(assignableTypes = {
        AuthViewController.class, CanchaViewController.class, ReservaViewController.class,
        ClaseViewController.class, AdminViewController.class})
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final CatalogoService catalogo;

    @ModelAttribute("usuario")
    public AuthUser usuario(@AuthenticationPrincipal AuthUser usuario) {
        return usuario;
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin(@AuthenticationPrincipal AuthUser usuario) {
        return usuario != null && usuario.esAdmin();
    }

    /** Filtros del dashboard y de clases: "todos" + deportes activos de la tabla "deportes". */
    @ModelAttribute("deportes")
    public Map<String, String> deportes() {
        return catalogo.mapaDeportes();
    }

    /** Lista desplegable de deportes (CRUD de canchas y clases) cargada desde la tabla "deportes". */
    @ModelAttribute("deportesDb")
    public List<Deporte> deportesDb() {
        return catalogo.deportes();
    }

    /** Lista desplegable de niveles (CRUD de clases) cargada desde la tabla "niveles_clase". */
    @ModelAttribute("niveles")
    public List<NivelClase> niveles() {
        return catalogo.niveles();
    }

    @ModelAttribute("bloques")
    public List<String> bloques() {
        return Horarios.BLOQUES;
    }

    @ModelAttribute("hoy")
    public String hoy() {
        return Horarios.hoy().toString();
    }

    @ModelAttribute("localeEs")
    public java.util.Locale localeEs() {
        return Fmt.ES_PE;
    }
}

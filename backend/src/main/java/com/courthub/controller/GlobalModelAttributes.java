package com.courthub.controller;

import com.courthub.security.AuthUser;
import com.courthub.util.Fmt;
import com.courthub.util.Horarios;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Atributos disponibles en TODAS las vistas Thymeleaf (header, filtros, fechas):
 * ${usuario}, ${esAdmin}, ${deportes}, ${bloques}, ${hoy}.
 */
@ControllerAdvice(assignableTypes = {
        AuthViewController.class, CanchaViewController.class, ReservaViewController.class,
        ClaseViewController.class, AdminViewController.class})
public class GlobalModelAttributes {

    private static final Map<String, String> DEPORTES = new LinkedHashMap<>();
    static {
        DEPORTES.put("todos", "Todos");
        DEPORTES.put("futbol", "Fútbol");
        DEPORTES.put("tenis", "Tenis");
        DEPORTES.put("squash", "Squash");
        DEPORTES.put("padel", "Pádel");
    }

    @ModelAttribute("usuario")
    public AuthUser usuario(@AuthenticationPrincipal AuthUser usuario) {
        return usuario;
    }

    @ModelAttribute("esAdmin")
    public boolean esAdmin(@AuthenticationPrincipal AuthUser usuario) {
        return usuario != null && usuario.esAdmin();
    }

    @ModelAttribute("deportes")
    public Map<String, String> deportes() {
        return DEPORTES;
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

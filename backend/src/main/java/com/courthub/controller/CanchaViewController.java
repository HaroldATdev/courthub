package com.courthub.controller;

import com.courthub.dto.CanchaDto;
import com.courthub.service.CanchaService;
import com.courthub.service.CatalogoService;
import com.courthub.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/** Vistas de canchas: dashboard con filtros (GET) y detalle con calendario de bloques. */
@Controller
@RequiredArgsConstructor
public class CanchaViewController {

    private final CanchaService canchaService;
    private final CatalogoService catalogoService;

    /** Dashboard: los filtros viajan como parámetros GET y el listado se renderiza en el servidor. */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "todos") String tipo,
                            @RequestParam(defaultValue = "") String q,
                            @RequestParam(defaultValue = "") String ubicacion,
                            @RequestParam(defaultValue = "") String precio,
                            Model model) {
        // Si llegara repetido (?tipo=a&tipo=b) Spring lo une con comas: nos quedamos con el primero válido
        if (tipo.contains(",")) tipo = tipo.split(",")[0];
        if (!tipo.equals("todos") && !catalogoService.existeDeporte(tipo)) tipo = "todos";
        String precioMin = null, precioMax = null;
        if (!precio.isBlank() && precio.contains("-")) {
            String[] p = precio.split("-");
            precioMin = p[0];
            precioMax = p[1];
        }
        List<CanchaDto> canchas = canchaService.listar(tipo, q, ubicacion, precioMin, precioMax);
        model.addAttribute("canchas", canchas);
        model.addAttribute("comunas", canchaService.comunas());
        model.addAttribute("tipo", tipo);
        model.addAttribute("q", q);
        model.addAttribute("ubicacion", ubicacion);
        model.addAttribute("precio", precio);
        return "dashboard";
    }

    /** Ícono (js/icons.js) de cada servicio conocido; el resto usa checkCircle. */
    private static final Map<String, String> ICONOS_SERVICIO = Map.of(
            "Estacionamiento", "car", "Duchas de agua caliente", "droplet", "Iluminación LED", "zap",
            "Vestuarios", "users", "Cafetería & Bar", "coffee", "WiFi gratuito", "wifi");

    @GetMapping("/canchas/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        CanchaDto cancha = canchaService.detalle(id);
        model.addAttribute("cancha", cancha);
        model.addAttribute("iconos", ICONOS_SERVICIO);
        model.addAttribute("cotizacion", ReservaService.Cotizacion.de(cancha.precioHora()));
        return "cancha-detalle";
    }
}

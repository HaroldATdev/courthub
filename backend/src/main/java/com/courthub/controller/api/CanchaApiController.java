package com.courthub.controller.api;

import com.courthub.dto.CanchaDto;
import com.courthub.service.CanchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** /api/canchas — listado con filtros, detalle y disponibilidad (requiere sesión). */
@RestController
@RequestMapping("/api/canchas")
@RequiredArgsConstructor
public class CanchaApiController {

    private final CanchaService service;

    @GetMapping
    public Map<String, List<CanchaDto>> listar(@RequestParam(required = false) String tipo,
                                               @RequestParam(required = false) String q,
                                               @RequestParam(required = false) String comuna,
                                               @RequestParam(required = false) String precioMin,
                                               @RequestParam(required = false) String precioMax) {
        return Map.of("canchas", service.listar(tipo, q, comuna, precioMin, precioMax));
    }

    @GetMapping("/{id}")
    public Map<String, CanchaDto> detalle(@PathVariable Long id) {
        return Map.of("cancha", service.detalle(id));
    }

    @GetMapping("/{id}/disponibilidad")
    public Map<String, Object> disponibilidad(@PathVariable Long id, @RequestParam(required = false) String fecha) {
        return service.disponibilidad(id, fecha);
    }
}

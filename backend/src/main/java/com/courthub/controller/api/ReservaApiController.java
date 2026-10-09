package com.courthub.controller.api;

import com.courthub.dto.ReservaDtos.*;
import com.courthub.security.AuthUser;
import com.courthub.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** /api/reservas — reservas del usuario autenticado. */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaApiController {

    private final ReservaService service;

    /** Reservas propias; el administrador puede ver todas con ?todas=1. */
    @GetMapping
    public Map<String, List<ReservaDto>> listar(@AuthenticationPrincipal AuthUser user,
                                                @RequestParam(required = false) String todas) {
        boolean verTodas = "1".equals(todas) && user.esAdmin();
        return Map.of("reservas", verTodas ? service.listarTodas() : service.listar(user.id()));
    }

    @PostMapping
    public ResponseEntity<Map<String, ReservaDto>> crear(@AuthenticationPrincipal AuthUser user,
                                                         @RequestBody CrearRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("reserva", service.crear(user.id(), req)));
    }

    @PatchMapping("/{id}/cancelar")
    public Map<String, ReservaDto> cancelar(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return Map.of("reserva", service.cancelar(user.id(), id));
    }

    @PatchMapping("/{id}/reprogramar")
    public Map<String, ReservaDto> reprogramar(@AuthenticationPrincipal AuthUser user, @PathVariable Long id,
                                               @RequestBody ReprogramarRequest req) {
        return Map.of("reserva", service.reprogramar(user.id(), id, req));
    }
}

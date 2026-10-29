package com.courthub.controller.api;
import com.courthub.service.AdminService;

import com.courthub.dto.AdminDtos.*;
import com.courthub.dto.CanchaDto;
import com.courthub.dto.ClaseDto;
import com.courthub.dto.ReservaDtos.ReservaDto;
import com.courthub.security.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * /api/admin/** — Panel ERP. Todas las rutas exigen rol "administrador" (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminService service;

    /* ---------- Dashboard ---------- */

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return service.dashboard();
    }

    /* ---------- Usuarios ---------- */

    @GetMapping("/usuarios")
    public Map<String, List<UsuarioAdminDto>> usuarios(@RequestParam(required = false) String q,
                                                       @RequestParam(required = false) String rol,
                                                       @RequestParam(required = false) String estado) {
        return Map.of("usuarios", service.usuarios(q, rol, estado));
    }

    @GetMapping("/entrenadores")
    public Map<String, List<UsuarioAdminDto>> entrenadores() {
        return Map.of("entrenadores", service.entrenadores());
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Map<String, UsuarioAdminDto>> crearUsuario(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("usuario", service.crearUsuario(req)));
    }

    @PutMapping("/usuarios/{id}")
    public Map<String, UsuarioAdminDto> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody UsuarioRequest req) {
        return Map.of("usuario", service.actualizarUsuario(id, req));
    }

    @PatchMapping("/usuarios/{id}/estado")
    public Map<String, UsuarioAdminDto> estadoUsuario(@AuthenticationPrincipal AuthUser admin, @PathVariable Long id,
                                                      @RequestBody EstadoRequest req) {
        return Map.of("usuario", service.estadoUsuario(admin, id, req.estado()));
    }

    @DeleteMapping("/usuarios/{id}")
    public Map<String, Object> eliminarUsuario(@AuthenticationPrincipal AuthUser admin, @PathVariable Long id) {
        service.eliminarUsuario(admin, id);
        return Map.of("ok", true, "mensaje", "Usuario anulado.");
    }

    /* ---------- Canchas ---------- */

    @GetMapping("/canchas")
    public Map<String, List<CanchaDto>> canchas() {
        return Map.of("canchas", service.canchas());
    }

    @PostMapping("/canchas")
    public ResponseEntity<Map<String, CanchaDto>> crearCancha(@Valid @RequestBody CanchaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("cancha", service.crearCancha(req)));
    }

    @PutMapping("/canchas/{id}")
    public Map<String, CanchaDto> actualizarCancha(@PathVariable Long id, @Valid @RequestBody CanchaRequest req) {
        return Map.of("cancha", service.actualizarCancha(id, req));
    }

    @PatchMapping("/canchas/{id}/estado")
    public Map<String, CanchaDto> estadoCancha(@PathVariable Long id, @RequestBody(required = false) EstadoRequest req) {
        return Map.of("cancha", service.estadoCancha(id, req == null ? new EstadoRequest(null, null, null) : req));
    }

    /* ---------- Reservas ---------- */

    @GetMapping("/reservas")
    public Map<String, List<ReservaDto>> reservas(@RequestParam(required = false) String estado,
                                                  @RequestParam(required = false) String desde,
                                                  @RequestParam(required = false) String hasta,
                                                  @RequestParam(required = false) Long canchaId,
                                                  @RequestParam(required = false) String q) {
        return Map.of("reservas", service.reservas(estado, desde, hasta, canchaId, q));
    }

    @PatchMapping("/reservas/{id}/estado")
    public Map<String, ReservaDto> estadoReserva(@PathVariable Long id, @RequestBody EstadoRequest req) {
        return Map.of("reserva", service.estadoReserva(id, req));
    }

    /* ---------- Clases ---------- */

    @GetMapping("/clases")
    public Map<String, List<ClaseDto>> clases() {
        return Map.of("clases", service.clases());
    }

    @PostMapping("/clases")
    public ResponseEntity<Map<String, ClaseDto>> crearClase(@Valid @RequestBody ClaseRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("clase", service.crearClase(req)));
    }

    @PutMapping("/clases/{id}")
    public Map<String, ClaseDto> actualizarClase(@PathVariable Long id, @Valid @RequestBody ClaseRequest req) {
        return Map.of("clase", service.actualizarClase(id, req));
    }

    @PatchMapping("/clases/{id}/estado")
    public Map<String, ClaseDto> estadoClase(@PathVariable Long id, @RequestBody(required = false) EstadoRequest req) {
        return Map.of("clase", service.estadoClase(id, req == null ? new EstadoRequest(null, null, null) : req));
    }

    @GetMapping("/clases/{id}/inscripciones")
    public Map<String, List<InscripcionDto>> inscripcionesDeClase(@PathVariable Long id) {
        return Map.of("inscripciones", service.inscripcionesDeClase(id));
    }

    /* ---------- Inscripciones ---------- */

    @GetMapping("/inscripciones")
    public Map<String, List<InscripcionDto>> inscripciones(@RequestParam(required = false) String estado) {
        return Map.of("inscripciones", service.inscripciones(estado));
    }

    @PatchMapping("/inscripciones/{id}/estado")
    public Map<String, InscripcionDto> estadoInscripcion(@PathVariable Long id, @RequestBody EstadoRequest req) {
        return Map.of("inscripcion", service.estadoInscripcion(id, req));
    }
}

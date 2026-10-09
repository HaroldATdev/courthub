package com.courthub.controller.api;

import com.courthub.dto.ClaseDto;
import com.courthub.security.AuthUser;
import com.courthub.service.ClaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** /api/clases — clases disponibles e inscripción/anulación. */
@RestController
@RequestMapping("/api/clases")
@RequiredArgsConstructor
public class ClaseApiController {

    private final ClaseService service;

    @GetMapping
    public Map<String, List<ClaseDto>> listar(@AuthenticationPrincipal AuthUser user,
                                              @RequestParam(required = false) String deporte) {
        return Map.of("clases", service.listar(user.id(), deporte));
    }

    @PostMapping("/{id}/inscribirse")
    public ResponseEntity<Map<String, Object>> inscribirse(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.inscribirse(user.id(), id));
    }

    @DeleteMapping("/{id}/inscribirse")
    public Map<String, Object> abandonar(@AuthenticationPrincipal AuthUser user, @PathVariable Long id) {
        return service.abandonar(user.id(), id);
    }
}

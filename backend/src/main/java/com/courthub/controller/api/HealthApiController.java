package com.courthub.controller.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/** GET /api/health — comprobación rápida de que la API está viva. */
@RestController
public class HealthApiController {

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of(
                "ok", true,
                "servicio", "courthub-api",
                "framework", "Spring Boot",
                "ts", OffsetDateTime.now().toString());
    }
}

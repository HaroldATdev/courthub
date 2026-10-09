package com.courthub.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

/**
 * Cookie HttpOnly que transporta el JWT en la navegación por vistas (MVC).
 * La API REST sigue aceptando el header "Authorization: Bearer <token>".
 */
public final class TokenCookie {

    private TokenCookie() {}

    public static final String NOMBRE = "CH_TOKEN";

    public static ResponseCookie crear(String token, long horas) {
        return ResponseCookie.from(NOMBRE, token)
                .httpOnly(true).path("/").sameSite("Lax")
                .maxAge(Duration.ofHours(horas)).build();
    }

    public static ResponseCookie borrar() {
        return ResponseCookie.from(NOMBRE, "").httpOnly(true).path("/").sameSite("Lax").maxAge(0).build();
    }

    public static String leer(HttpServletRequest req) {
        if (req.getCookies() == null) return null;
        for (Cookie c : req.getCookies()) {
            if (NOMBRE.equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) return c.getValue();
        }
        return null;
    }
}

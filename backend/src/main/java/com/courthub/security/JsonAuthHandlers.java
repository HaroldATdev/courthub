package com.courthub.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Respuestas JSON para 401 (sin token) y 403 (sin permisos), en lugar de las páginas HTML por defecto. */
public final class JsonAuthHandlers {

    private JsonAuthHandlers() {}

    public static void escribir(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"error\":\"" + mensaje.replace("\"", "'") + "\"}");
    }

    public static AuthenticationEntryPoint entryPoint() {
        return (req, res, e) -> escribir(res, HttpServletResponse.SC_UNAUTHORIZED, "Token de autenticación requerido.");
    }

    public static AccessDeniedHandler accessDenied() {
        return (req, res, e) -> escribir(res, HttpServletResponse.SC_FORBIDDEN, "No tienes permisos para esta acción.");
    }
}

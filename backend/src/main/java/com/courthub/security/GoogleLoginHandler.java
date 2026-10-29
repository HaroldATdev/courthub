package com.courthub.security;

import com.courthub.exception.ApiException;
import com.courthub.model.Usuario;
import com.courthub.service.AuthService;
import com.courthub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Qué pasa cuando Google confirma la identidad del usuario:
 *  1. Se busca la cuenta por correo; si no existe se crea (perfil cliente, estado "pendiente").
 *  2. Si la cuenta está activa se emite el JWT en la cookie y se redirige según el perfil;
 *     si está pendiente o anulada se vuelve al login con el aviso correspondiente.
 * La sesión creada por Spring Security durante el flujo OAuth2 se invalida: la autenticación
 * del sistema es siempre la cookie JWT (igual que el login por formulario).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleLoginHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

    private final AuthService authService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User google = (OAuth2User) authentication.getPrincipal();
        String email = google.getAttribute("email");
        String nombres = google.getAttribute("given_name");
        String apellidos = google.getAttribute("family_name");
        String nombreCompleto = google.getAttribute("name");
        if (nombres == null) nombres = nombreCompleto == null ? "Usuario" : nombreCompleto;

        invalidarSesion(request);
        if (email == null || Boolean.FALSE.equals(google.getAttribute("email_verified"))) {
            response.sendRedirect("/login?google=error");
            return;
        }
        try {
            Usuario u = authService.loginConGoogle(email, nombres, apellidos == null ? "" : apellidos);
            response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.crear(jwtService.generar(u), jwtService.getHorasExpiracion()).toString());
            response.sendRedirect(AuthUser.ADMINISTRADOR.equals(u.getRol()) ? "/admin" : "/dashboard");
        } catch (ApiException e) {
            String motivo = e.getMessage().contains("anulada") ? "anulada" : "pendiente";
            response.sendRedirect("/login?google=" + motivo);
        }
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        log.warn("[Google] Autenticación fallida: {}", exception.getMessage());
        invalidarSesion(request);
        response.sendRedirect("/login?google=error");
    }

    private static void invalidarSesion(HttpServletRequest request) {
        if (request.getSession(false) != null) request.getSession(false).invalidate();
    }
}

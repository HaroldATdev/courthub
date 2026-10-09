package com.courthub.security;

import com.courthub.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Autentica cada petición a partir del JWT:
 *  - Header "Authorization: Bearer <token>" (clientes de la API).
 *  - Cookie CH_TOKEN (navegación por las vistas). Para rutas /api/** la cookie solo se acepta
 *    si viene el header X-Requested-With (lo envía js/api.js), como protección anti-CSRF.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        boolean esApi = request.getRequestURI().startsWith(request.getContextPath() + "/api/");
        String token = null;

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else if (!esApi || request.getHeader("X-Requested-With") != null) {
            token = TokenCookie.leer(request);
        }

        if (token != null) {
            try {
                AuthUser usuario = jwtService.parsear(token);
                var auth = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                if (esApi) {
                    JsonAuthHandlers.escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido o expirado.");
                    return;
                }
                // Cookie caducada en la navegación web: se limpia y la ruta protegida redirige al login
                response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.borrar().toString());
            }
        }
        chain.doFilter(request, response);
    }
}

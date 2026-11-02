package com.courthub.security;

import com.courthub.model.Usuario;
import com.courthub.repository.UsuarioRepository;
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
 *
 * El token solo identifica al usuario: en cada petición se consulta la BD y la sesión solo es válida
 * si la cuenta sigue en estado "activo". Así, anular a un usuario cierra su sesión de inmediato (no
 * hay que esperar a que caduque el JWT) y los cambios de perfil o de rol se aplican al instante.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarios;

    public JwtAuthFilter(JwtService jwtService, UsuarioRepository usuarios) {
        this.jwtService = jwtService;
        this.usuarios = usuarios;
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
                AuthUser jwt = jwtService.parsear(token);
                Usuario u = usuarios.findById(jwt.id()).filter(Usuario::estaActivo)
                        .orElseThrow(() -> new IllegalStateException("cuenta inactiva o inexistente"));
                AuthUser usuario = new AuthUser(u.getId(), u.getEmail(), u.getRol(), u.getNombre(), u.getApellido(),
                        u.getNivel(), u.getDeporteFavorito());
                var auth = new UsernamePasswordAuthenticationToken(
                        usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().toUpperCase())));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                if (esApi) {
                    JsonAuthHandlers.escribir(response, HttpServletResponse.SC_UNAUTHORIZED, "Token inválido, expirado o cuenta no activa.");
                    return;
                }
                // Cookie caducada o cuenta anulada en la navegación web: se limpia y la ruta protegida redirige al login
                request.getSession().invalidate();
                response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.borrar().toString());
            }
        }
        chain.doFilter(request, response);
    }
}

package com.courthub.service;

import com.courthub.dto.AuthDtos.*;
import com.courthub.exception.ApiException;
import com.courthub.model.Usuario;
import com.courthub.repository.UsuarioRepository;
import com.courthub.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/** Registro, login y perfil. Los administradores solo se crean por seed o desde el panel admin. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Set<String> ROLES_REGISTRO = Set.of(AuthUser.CLIENTE, AuthUser.ENTRENADOR);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public AuthResponse registrar(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo.");
        }
        // El formulario envía "Nombre Apellido" en un solo campo: lo separamos.
        String nombre = req.nombre().trim();
        String apellido = req.apellido() == null ? "" : req.apellido().trim();
        if (apellido.isEmpty() && nombre.contains(" ")) {
            int i = nombre.indexOf(' ');
            apellido = nombre.substring(i + 1).trim();
            nombre = nombre.substring(0, i);
        }
        String rol = ROLES_REGISTRO.contains(req.rol()) ? req.rol() : AuthUser.CLIENTE;

        Usuario u = Usuario.builder()
                .nombre(nombre).apellido(apellido).email(email)
                .passwordHash(encoder.encode(req.password()))
                .rol(rol).build();
        u = usuarios.save(u);
        return new AuthResponse(jwt.generar(u), UsuarioDto.de(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByEmailIgnoreCase(req.email().trim())
                .filter(Usuario::getActivo)
                .orElseThrow(() -> ApiException.unauthorized("Credenciales incorrectas."));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciales incorrectas.");
        }
        return new AuthResponse(jwt.generar(u), UsuarioDto.de(u));
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long id) {
        return usuarios.findById(id).map(UsuarioDto::de)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }
}

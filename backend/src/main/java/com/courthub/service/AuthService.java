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

/**
 * Registro y login (tercer avance).
 *  - Registro: valida DNI y usuario únicos; el perfil puede ser cliente o administrador;
 *    la cuenta nace en estado "pendiente" y solo el administrador la activa.
 *  - Login: por usuario o correo contra la BD; solo entran cuentas en estado "activo".
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String MSG_PENDIENTE = "Tu cuenta está pendiente de activación por el administrador.";
    public static final String MSG_ANULADA = "Tu cuenta ha sido anulada. Contacta al administrador.";
    public static final String MSG_REGISTRO_OK = "Registro exitoso. Tu cuenta quedará activa cuando el administrador la apruebe.";

    private static final Set<String> PERFILES_REGISTRO = Set.of(AuthUser.CLIENTE, AuthUser.ADMINISTRADOR);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public RegisterResponse registrar(RegisterRequest req) {
        String dni = req.dni().trim();
        String usuario = req.usuario().trim();
        String email = req.email().trim().toLowerCase();

        // a) Validación por DNI
        if (usuarios.existsByDni(dni)) {
            throw ApiException.conflict("El DNI " + dni + " ya está registrado.");
        }
        // b) Validación por usuario
        if (usuarios.existsByUsuarioIgnoreCase(usuario)) {
            throw ApiException.conflict("El nombre de usuario \"" + usuario + "\" ya está en uso.");
        }
        if (usuarios.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Ya existe una cuenta con ese correo.");
        }

        // Si no llega apellido y el nombre viene como "Nombre Apellido", se separa.
        String nombre = req.nombre().trim();
        String apellido = req.apellido() == null ? "" : req.apellido().trim();
        if (apellido.isEmpty() && nombre.contains(" ")) {
            int i = nombre.indexOf(' ');
            apellido = nombre.substring(i + 1).trim();
            nombre = nombre.substring(0, i);
        }
        String rol = PERFILES_REGISTRO.contains(req.rol()) ? req.rol() : AuthUser.CLIENTE;

        Usuario u = Usuario.builder()
                .dni(dni).usuario(usuario)
                .nombre(nombre).apellido(apellido).email(email)
                .passwordHash(encoder.encode(req.password()))
                .rol(rol).estado(Usuario.PENDIENTE)
                .build();
        u = usuarios.save(u);
        return new RegisterResponse(MSG_REGISTRO_OK, UsuarioDto.de(u));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        Usuario u = usuarios.findByUsuarioOEmail(req.usuario().trim())
                .orElseThrow(() -> ApiException.unauthorized("Credenciales incorrectas."));
        if (!encoder.matches(req.password(), u.getPasswordHash())) {
            throw ApiException.unauthorized("Credenciales incorrectas.");
        }
        if (Usuario.PENDIENTE.equals(u.getEstado())) throw ApiException.forbidden(MSG_PENDIENTE);
        if (Usuario.ANULADO.equals(u.getEstado())) throw ApiException.forbidden(MSG_ANULADA);
        return new AuthResponse(jwt.generar(u), UsuarioDto.de(u));
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long id) {
        return usuarios.findById(id).map(UsuarioDto::de)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }
}

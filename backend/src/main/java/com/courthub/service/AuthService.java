package com.courthub.service;

import com.courthub.dto.AuthDtos.*;
import com.courthub.dto.form.Formularios.PerfilForm;
import com.courthub.exception.ApiException;
import com.courthub.model.Usuario;
import com.courthub.repository.UsuarioRepository;
import com.courthub.security.AuthUser;
import com.courthub.util.Telefono;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Registro y login (tercer avance).
 *  - Registro: valida DNI y usuario únicos; el perfil puede ser cliente o administrador;
 *    los clientes nacen "activos"; el perfil administrador nace "pendiente" hasta que otro administrador lo aprueba.
 *  - Login: por usuario o correo contra la BD; solo entran cuentas en estado "activo".
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    public static final String MSG_PENDIENTE = "Tu cuenta está pendiente de activación por el administrador.";
    public static final String MSG_ANULADA = "Tu cuenta ha sido anulada. Contacta al administrador.";
    public static final String MSG_REGISTRO_OK = "Registro exitoso. Tu cuenta ya está activa: inicia sesión.";
    public static final String MSG_REGISTRO_ADMIN = "Registro exitoso. Una cuenta de administrador queda pendiente hasta que otro administrador la apruebe.";

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
        // Los clientes entran activos; quien pide el perfil de administrador queda pendiente hasta que
        // otro administrador lo apruebe (si no, cualquiera podría darse acceso total al panel).
        String estado = AuthUser.ADMINISTRADOR.equals(rol) ? Usuario.PENDIENTE : Usuario.ACTIVO;

        Usuario u = Usuario.builder()
                .dni(dni).usuario(usuario)
                .nombre(nombre).apellido(apellido).email(email)
                .passwordHash(encoder.encode(req.password()))
                .rol(rol).estado(estado)
                .build();
        u = usuarios.save(u);
        return new RegisterResponse(AuthUser.ADMINISTRADOR.equals(rol) ? MSG_REGISTRO_ADMIN : MSG_REGISTRO_OK, UsuarioDto.de(u));
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

    /**
     * Inicio de sesión con Google: la identidad la confirma Google (OpenID Connect).
     * Si el correo no existe se crea la cuenta como cliente en estado "activo" (Google ya verificó el correo).
     * Una cuenta pendiente o anulada sigue sin entrar. El perfil administrador nunca se concede por esta vía.
     * noRollbackFor: la excepción "pendiente/anulada" es solo un aviso para el usuario y no debe deshacer la transacción.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Usuario loginConGoogle(String email, String nombres, String apellidos) {
        String correo = email.trim().toLowerCase();
        Usuario u = usuarios.findByEmailIgnoreCase(correo).orElseGet(() -> {
            String base = correo.substring(0, correo.indexOf('@')).replaceAll("[^A-Za-z0-9._-]", "");
            String usuario = base.isEmpty() ? "google" : base;
            int i = 1;
            while (usuarios.existsByUsuarioIgnoreCase(usuario)) usuario = base + (++i);
            return usuarios.save(Usuario.builder()
                    .usuario(usuario).nombre(nombres.trim()).apellido(apellidos.trim()).email(correo)
                    .passwordHash(encoder.encode(java.util.UUID.randomUUID().toString())) // sin clave local: entra solo con Google
                    .rol(AuthUser.CLIENTE).estado(Usuario.ACTIVO)
                    .build());
        });
        if (Usuario.PENDIENTE.equals(u.getEstado())) throw ApiException.forbidden(MSG_PENDIENTE);
        if (Usuario.ANULADO.equals(u.getEstado())) throw ApiException.forbidden(MSG_ANULADA);
        return u;
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Long id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }

    /**
     * "Mi perfil": el usuario completa/corrige sus datos. Mantiene las mismas reglas de unicidad del
     * registro (DNI y nombre de usuario); el correo y la clave no se modifican aquí.
     */
    @Transactional
    public Usuario actualizarPerfil(Long id, PerfilForm f) {
        Usuario u = obtener(id);
        String dni = f.getDni().trim();
        String usuario = f.getUsuario().trim();
        if (!dni.equals(u.getDni()) && usuarios.existsByDni(dni)) {
            throw ApiException.conflict("El DNI " + dni + " ya está registrado por otra cuenta.");
        }
        if (!usuario.equalsIgnoreCase(u.getUsuario()) && usuarios.existsByUsuarioIgnoreCase(usuario)) {
            throw ApiException.conflict("El nombre de usuario \"" + usuario + "\" ya está en uso.");
        }
        u.setDni(dni);
        u.setUsuario(usuario);
        u.setNombre(f.getNombre().trim());
        u.setApellido(f.getApellido().trim());
        u.setTelefono(Telefono.normalizar(f.getTelefono()));
        u.setNivel(f.getNivel() == null || f.getNivel().isBlank() ? "Bronce" : f.getNivel().trim());
        u.setDeporteFavorito(f.getDeporteFavorito() == null || f.getDeporteFavorito().isBlank() ? "Multi" : f.getDeporteFavorito().trim());
        return usuarios.save(u);
    }

    @Transactional(readOnly = true)
    public UsuarioDto perfil(Long id) {
        return usuarios.findById(id).map(UsuarioDto::de)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
    }
}

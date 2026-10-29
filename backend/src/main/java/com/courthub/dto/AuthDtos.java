package com.courthub.dto;

import com.courthub.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTOs de autenticación (entrada y salida). */
public final class AuthDtos {

    private AuthDtos() {}

    /** Registro (tercer avance): dni, nombres, apellidos, usuario, clave, perfil. El estado inicial es "pendiente". */
    public record RegisterRequest(
            @NotBlank(message = "El DNI es obligatorio.")
            @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos.") String dni,
            @NotBlank(message = "Los nombres son obligatorios.") String nombre,
            String apellido,
            @NotBlank(message = "El nombre de usuario es obligatorio.")
            @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres.") String usuario,
            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "El correo no es válido.") String email,
            @NotBlank(message = "La contraseña es obligatoria.")
            @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String password,
            String rol) {}

    /** Login: "usuario" acepta el nombre de usuario o el correo. */
    public record LoginRequest(
            @NotBlank(message = "Ingresa tu usuario y contraseña.") String usuario,
            @NotBlank(message = "Ingresa tu usuario y contraseña.") String password) {}

    /** Proyección pública del usuario (nunca expone el hash). */
    public record UsuarioDto(Long id, String dni, String usuario, String nombre, String apellido, String email,
                             String rol, String estado, String nivel, String deporte, String telefono) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getDni(), u.getUsuario(), u.getNombre(), u.getApellido(), u.getEmail(),
                    u.getRol(), u.getEstado(), u.getNivel(), u.getDeporteFavorito(), u.getTelefono());
        }
    }

    public record AuthResponse(String token, UsuarioDto usuario) {}

    /** Respuesta del registro: la cuenta queda pendiente, por eso no se emite token. */
    public record RegisterResponse(String mensaje, UsuarioDto usuario) {}
}

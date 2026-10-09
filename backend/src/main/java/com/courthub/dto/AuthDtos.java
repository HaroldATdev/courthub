package com.courthub.dto;

import com.courthub.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** DTOs de autenticación (entrada y salida). */
public final class AuthDtos {

    private AuthDtos() {}

    public record RegisterRequest(
            @NotBlank(message = "Nombre, correo y contraseña son obligatorios.") String nombre,
            String apellido,
            @NotBlank(message = "Nombre, correo y contraseña son obligatorios.")
            @Email(message = "El correo no es válido.") String email,
            @NotBlank(message = "Nombre, correo y contraseña son obligatorios.")
            @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") String password,
            String rol) {}

    public record LoginRequest(
            @NotBlank(message = "Ingresa tu correo y contraseña.") String email,
            @NotBlank(message = "Ingresa tu correo y contraseña.") String password) {}

    /** Proyección pública del usuario (nunca expone el hash). */
    public record UsuarioDto(Long id, String nombre, String apellido, String email, String rol,
                             String nivel, String deporte, String telefono) {
        public static UsuarioDto de(Usuario u) {
            return new UsuarioDto(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol(),
                    u.getNivel(), u.getDeporteFavorito(), u.getTelefono());
        }
    }

    public record AuthResponse(String token, UsuarioDto usuario) {}
}

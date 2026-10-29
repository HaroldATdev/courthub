package com.courthub.dto;
import com.courthub.model.Inscripcion;

import com.courthub.model.Inscripcion;
import com.courthub.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.List;

/** DTOs del panel de administración (ERP). */
public final class AdminDtos {

    private AdminDtos() {}

    /* ---------------- Usuarios ---------------- */

    /** Alta/edición de usuarios desde el CRUD del administrador (dni, nombres, apellidos, usuario, clave, perfil, estado). */
    public record UsuarioRequest(@NotBlank(message = "El DNI es obligatorio.")
                                 @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos.") String dni,
                                 @NotBlank(message = "El nombre es obligatorio.") String nombre,
                                 String apellido,
                                 @NotBlank(message = "El nombre de usuario es obligatorio.") String usuario,
                                 @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no es válido.") String email,
                                 String password,          // obligatorio al crear, opcional al editar
                                 String rol, String estado, String telefono, String nivel, String deporteFavorito) {}

    public record UsuarioAdminDto(Long id, String dni, String usuario, String nombre, String apellido, String email, String rol,
                                  String estado, String telefono, String nivel, String deporte, String fechaRegistro) {
        public static UsuarioAdminDto de(Usuario u) {
            return new UsuarioAdminDto(u.getId(), u.getDni(), u.getUsuario(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol(),
                    u.getEstado(), u.getTelefono(), u.getNivel(), u.getDeporteFavorito(),
                    u.getFechaRegistro() == null ? null : u.getFechaRegistro().toString());
        }
        public boolean activo() { return Usuario.ACTIVO.equals(estado); }
    }

    /* ---------------- Canchas ---------------- */

    public record CanchaRequest(@NotBlank(message = "El nombre es obligatorio.") String nombre,
                                @NotBlank(message = "El tipo de deporte es obligatorio.") String tipo,
                                String subtipo, String descripcion, String direccion,
                                @NotBlank(message = "La comuna/distrito es obligatoria.") String comuna,
                                String ciudad,
                                @NotNull(message = "El precio por hora es obligatorio.") BigDecimal precioHora,
                                BigDecimal calificacion, Integer opiniones, String imagen, Boolean verificada,
                                List<String> servicios, List<String> reglamento, Boolean activa) {}

    /* ---------------- Clases ---------------- */

    public record ClaseRequest(@NotBlank(message = "El nombre es obligatorio.") String nombre,
                               @NotBlank(message = "El deporte es obligatorio.") String deporte,
                               String nivel, String descripcion,
                               @NotNull(message = "Selecciona un entrenador.") Long entrenadorId,
                               Long canchaId,
                               @NotBlank(message = "La fecha es obligatoria.") String fecha,
                               @NotBlank(message = "La hora de inicio es obligatoria.") String horaInicio,
                               @NotBlank(message = "La hora de fin es obligatoria.") String horaFin,
                               Integer cupoMaximo, BigDecimal precio, Boolean activa) {}

    /* ---------------- Cambios de estado ---------------- */

    public record EstadoRequest(String estado, Boolean activo, Boolean activa) {}

    /* ---------------- Inscripciones ---------------- */

    public record InscripcionDto(Long id, Long claseId, String clase, String deporte, String fechaClase,
                                 Long usuarioId, String alumno, String email, String estado, String fechaInscripcion) {
        public static InscripcionDto de(Inscripcion i) {
            return new InscripcionDto(i.getId(), i.getClase().getId(), i.getClase().getNombre(), i.getClase().getDeporte(),
                    i.getClase().getFecha().toString(), i.getUsuario().getId(), i.getUsuario().nombreCompleto(),
                    i.getUsuario().getEmail(), i.getEstado(),
                    i.getFechaInscripcion() == null ? null : i.getFechaInscripcion().toString());
        }
    }
}

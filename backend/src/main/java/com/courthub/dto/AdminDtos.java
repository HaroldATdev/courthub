package com.courthub.dto;
import com.courthub.model.Inscripcion;

import com.courthub.model.Inscripcion;
import com.courthub.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/** DTOs del panel de administración (ERP). */
public final class AdminDtos {

    private AdminDtos() {}

    /* ---------------- Usuarios ---------------- */

    public record UsuarioRequest(@NotBlank(message = "El nombre es obligatorio.") String nombre,
                                 String apellido,
                                 @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no es válido.") String email,
                                 String password,          // obligatorio al crear, opcional al editar
                                 String rol, String telefono, String nivel, String deporteFavorito, Boolean activo) {}

    public record UsuarioAdminDto(Long id, String nombre, String apellido, String email, String rol, String telefono,
                                  String nivel, String deporte, Boolean activo, String fechaRegistro) {
        public static UsuarioAdminDto de(Usuario u) {
            return new UsuarioAdminDto(u.getId(), u.getNombre(), u.getApellido(), u.getEmail(), u.getRol(), u.getTelefono(),
                    u.getNivel(), u.getDeporteFavorito(), u.getActivo(),
                    u.getFechaRegistro() == null ? null : u.getFechaRegistro().toString());
        }
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

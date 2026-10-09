package com.courthub.dto.form;

import com.courthub.dto.AdminDtos.CanchaRequest;
import com.courthub.dto.AdminDtos.ClaseRequest;
import com.courthub.dto.AdminDtos.UsuarioRequest;
import com.courthub.dto.CanchaDto;
import com.courthub.dto.ClaseDto;
import com.courthub.dto.AdminDtos.UsuarioAdminDto;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Objetos de formulario (form-backing beans) de las vistas Thymeleaf.
 * Son mutables (th:field) y se validan con Bean Validation + BindingResult en los controladores.
 */
public final class Formularios {

    private Formularios() {}

    @Data
    public static class LoginForm {
        @NotBlank(message = "Ingresa tu correo y contraseña para continuar.") private String email;
        @NotBlank(message = "Ingresa tu correo y contraseña para continuar.") private String password;
    }

    @Data
    public static class RegistroForm {
        @NotBlank(message = "Completa todos los campos.") private String nombre;
        @NotBlank(message = "Completa todos los campos.") @Email(message = "El correo no es válido.") private String email;
        @NotBlank(message = "Completa todos los campos.") @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.") private String password;
        private String password2;
        private String rol = "cliente";
    }

    /** Pago y confirmación de reserva. */
    @Data
    public static class ReservaForm {
        @NotNull private Long canchaId;
        @NotBlank private String fecha;
        @NotBlank private String horaInicio;
        private String metodoPago = "Tarjeta de Crédito";
        private String nombreTarjeta;
        private String numeroTarjeta;
        private String vencimiento;
        private String cvv;
        private boolean terminos;
    }

    @Data
    public static class ReprogramarForm {
        @NotBlank(message = "Selecciona una fecha válida (hoy o futura).") private String fecha;
        @NotBlank(message = "Selecciona un bloque horario.") private String horaInicio;
    }

    /* ---------------- Panel admin ---------------- */

    @Data
    public static class CanchaForm {
        @NotBlank(message = "El nombre es obligatorio.") private String nombre;
        @NotBlank(message = "El tipo de deporte es obligatorio.") private String tipo = "futbol";
        private String subtipo;
        private String descripcion;
        private String direccion;
        @NotBlank(message = "El distrito es obligatorio.") private String comuna;
        private String ciudad = "Lima";
        @NotNull(message = "El precio por hora es obligatorio.") @DecimalMin(value = "1", message = "El precio por hora debe ser mayor a 0.") private BigDecimal precioHora;
        @DecimalMin(value = "0", message = "La calificación debe estar entre 0 y 5.") @DecimalMax(value = "5", message = "La calificación debe estar entre 0 y 5.") private BigDecimal calificacion = new BigDecimal("4.5");
        @Min(value = 0, message = "Las opiniones no pueden ser negativas.") private Integer opiniones = 0;
        private String imagen;
        private boolean verificada = true;
        private String servicios;    // separados por coma
        private String reglamento;   // una regla por línea
        private boolean activa = true;

        public static CanchaForm de(CanchaDto c) {
            CanchaForm f = new CanchaForm();
            f.nombre = c.nombre(); f.tipo = c.tipo(); f.subtipo = c.subtipo(); f.descripcion = c.descripcion();
            f.direccion = c.direccion(); f.comuna = c.comuna(); f.ciudad = c.ciudad(); f.precioHora = c.precioHora();
            f.calificacion = c.calificacion(); f.opiniones = c.opiniones(); f.imagen = c.imagen();
            f.verificada = Boolean.TRUE.equals(c.verificada()); f.activa = Boolean.TRUE.equals(c.activa());
            f.servicios = String.join(", ", c.servicios()); f.reglamento = String.join("\n", c.reglamento());
            return f;
        }

        public CanchaRequest aRequest() {
            List<String> serv = servicios == null ? List.of() : Arrays.stream(servicios.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            List<String> regl = reglamento == null ? List.of() : Arrays.stream(reglamento.split("\\r?\\n")).map(String::trim).filter(s -> !s.isEmpty()).toList();
            return new CanchaRequest(nombre, tipo, subtipo, descripcion, direccion, comuna, ciudad, precioHora, calificacion,
                    opiniones, imagen, verificada, serv, regl, activa);
        }
    }

    @Data
    public static class ClaseForm {
        @NotBlank(message = "El nombre es obligatorio.") private String nombre;
        @NotBlank(message = "El deporte es obligatorio.") private String deporte = "futbol";
        private String nivel = "Todos los niveles";
        private String descripcion;
        @NotNull(message = "Selecciona un entrenador.") private Long entrenadorId;
        private Long canchaId;
        @NotBlank(message = "La fecha es obligatoria.") private String fecha;
        @NotBlank(message = "La hora de inicio es obligatoria.") private String horaInicio = "18:00";
        @NotBlank(message = "La hora de fin es obligatoria.") private String horaFin = "19:30";
        @NotNull @Min(value = 1, message = "El cupo máximo debe ser mayor a 0.") private Integer cupoMaximo = 12;
        @DecimalMin(value = "0", message = "El precio no puede ser negativo.") private BigDecimal precio = BigDecimal.ZERO;
        private boolean activa = true;

        public static ClaseForm de(ClaseDto k) {
            ClaseForm f = new ClaseForm();
            f.nombre = k.nombre(); f.deporte = k.deporte(); f.nivel = k.nivel(); f.descripcion = k.descripcion();
            f.entrenadorId = k.entrenador().id(); f.canchaId = k.cancha() == null ? null : k.cancha().id();
            f.fecha = k.fecha(); f.horaInicio = k.horaInicio(); f.horaFin = k.horaFin();
            f.cupoMaximo = k.cupoMax(); f.precio = k.precio(); f.activa = Boolean.TRUE.equals(k.activa());
            return f;
        }

        public ClaseRequest aRequest() {
            return new ClaseRequest(nombre, deporte, nivel, descripcion, entrenadorId, canchaId, fecha, horaInicio, horaFin, cupoMaximo, precio, activa);
        }
    }

    @Data
    public static class UsuarioForm {
        @NotBlank(message = "El nombre es obligatorio.") private String nombre;
        private String apellido;
        @NotBlank(message = "El correo es obligatorio.") @Email(message = "El correo no es válido.") private String email;
        private String password;
        private String rol = "cliente";
        private String telefono;
        private String nivel = "Bronce";
        private String deporteFavorito = "Multi";
        private boolean activo = true;

        public static UsuarioForm de(UsuarioAdminDto u) {
            UsuarioForm f = new UsuarioForm();
            f.nombre = u.nombre(); f.apellido = u.apellido(); f.email = u.email(); f.rol = u.rol(); f.telefono = u.telefono();
            f.nivel = u.nivel(); f.deporteFavorito = u.deporte(); f.activo = Boolean.TRUE.equals(u.activo());
            return f;
        }

        public UsuarioRequest aRequest() {
            return new UsuarioRequest(nombre, apellido, email, password, rol, telefono, nivel, deporteFavorito, activo);
        }
    }
}

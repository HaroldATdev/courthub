package com.courthub.dto;

import com.courthub.model.Reserva;
import com.courthub.util.Horarios;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

/** DTOs de reservas. Las peticiones de la API llegan en snake_case (cancha_id, hora_inicio...). */
public final class ReservaDtos {

    private ReservaDtos() {}

    public record CrearRequest(@JsonProperty("cancha_id") Long canchaId,
                               String fecha,
                               @JsonProperty("hora_inicio") String horaInicio,
                               @JsonProperty("hora_fin") String horaFin,
                               @JsonProperty("metodo_pago") String metodoPago) {}

    public record ReprogramarRequest(String fecha,
                                     @JsonProperty("hora_inicio") String horaInicio,
                                     @JsonProperty("hora_fin") String horaFin) {}

    public record ClienteSnap(Long id, String nombre, String email) {}

    public record ReservaDto(Long id, String codigo, Long canchaId, CanchaDto.Snap cancha, ClienteSnap cliente,
                             String fecha, String horaInicio, String horaFin, String estado,
                             BigDecimal subtotal, BigDecimal cargoServicio, BigDecimal total,
                             String metodoPago, String notas, String fechaCreacion) {

        public static ReservaDto de(Reserva r) {
            return de(r, false);
        }

        /** Con incluirCliente=true agrega nombre/email del usuario (vista de administrador). */
        public static ReservaDto de(Reserva r, boolean incluirCliente) {
            ClienteSnap cliente = null;
            if (incluirCliente && r.getUsuario() != null) {
                cliente = new ClienteSnap(r.getUsuario().getId(), r.getUsuario().nombreCompleto(), r.getUsuario().getEmail());
            }
            return new ReservaDto(r.getId(), r.getCodigo(), r.getCancha().getId(), CanchaDto.Snap.de(r.getCancha()), cliente,
                    r.getFecha().toString(), Horarios.hora(r.getHoraInicio()), Horarios.hora(r.getHoraFin()), r.getEstado(),
                    r.getSubtotal(), r.getCargoServicio(), r.getTotal(), r.getMetodoPago(), r.getNotas(),
                    r.getFechaCreacion() == null ? null : r.getFechaCreacion().toString());
        }

        /** Pestaña del historial: proximas | pasadas | canceladas (usado por la vista "Mis reservas"). */
        public String categoria(LocalDate hoy) {
            if (Reserva.CANCELADA.equals(estado)) return "canceladas";
            if (Reserva.COMPLETADA.equals(estado) || LocalDate.parse(fecha).isBefore(hoy)) return "pasadas";
            return "proximas";
        }

        public boolean esProxima(LocalDate hoy) { return "proximas".equals(categoria(hoy)); }
    }
}

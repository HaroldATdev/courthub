package com.courthub.controller;

import com.courthub.dto.CanchaDto;
import com.courthub.dto.ReservaDtos.ReservaDto;
import com.courthub.dto.form.Formularios.ReprogramarForm;
import com.courthub.dto.form.Formularios.ReservaForm;
import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.service.CanchaService;
import com.courthub.service.ReservaService;
import com.courthub.service.ReservaService.Cotizacion;
import com.courthub.util.Horarios;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Vistas de reservas: pago/confirmación y historial (cancelar, reprogramar). */
@Controller
@RequiredArgsConstructor
public class ReservaViewController {

    private final ReservaService reservaService;
    private final CanchaService canchaService;

    /* ---------------- Pago ---------------- */

    @GetMapping("/pago")
    public String pago(@RequestParam Long canchaId, @RequestParam String fecha, @RequestParam String hora,
                       Model model, RedirectAttributes ra) {
        LocalDate f = Horarios.parseFecha(fecha, "Fecha inválida.");
        if (!canchaService.bloqueDisponible(canchaId, f, hora)) {
            ra.addFlashAttribute("toast", "Ese bloque horario ya no está disponible. Elige otro.");
            ra.addFlashAttribute("toastTipo", "error");
            return "redirect:/canchas/" + canchaId;
        }
        ReservaForm form = new ReservaForm();
        form.setCanchaId(canchaId);
        form.setFecha(f.toString());
        form.setHoraInicio(hora);
        model.addAttribute("reservaForm", form);
        prepararPago(model, form);
        return "pago";
    }

    @PostMapping("/reservas")
    public String confirmar(@AuthenticationPrincipal AuthUser usuario, @Valid @ModelAttribute ReservaForm reservaForm,
                            BindingResult result, Model model, RedirectAttributes ra) {
        if (!reservaForm.isTerminos()) {
            result.rejectValue("terminos", "terminos", "Debes aceptar los términos y condiciones para continuar.");
        }
        if ("Tarjeta de Crédito".equals(reservaForm.getMetodoPago())) {
            String numero = digitos(reservaForm.getNumeroTarjeta());
            String venc = digitos(reservaForm.getVencimiento());
            String cvv = digitos(reservaForm.getCvv());
            if (reservaForm.getNombreTarjeta() == null || reservaForm.getNombreTarjeta().isBlank()) {
                result.rejectValue("nombreTarjeta", "tarjeta", "Ingresa el nombre que aparece en la tarjeta.");
            } else if (numero.length() != 16) {
                result.rejectValue("numeroTarjeta", "tarjeta", "El número de tarjeta debe tener 16 dígitos.");
            } else if (venc.length() != 4 || Integer.parseInt(venc.substring(0, 2)) < 1 || Integer.parseInt(venc.substring(0, 2)) > 12) {
                result.rejectValue("vencimiento", "tarjeta", "Ingresa una fecha de vencimiento válida (MM / AA).");
            } else if (cvv.length() < 3) {
                result.rejectValue("cvv", "tarjeta", "El CVV debe tener 3 o 4 dígitos.");
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("pagoError", result.getAllErrors().get(0).getDefaultMessage());
            prepararPago(model, reservaForm);
            return "pago";
        }
        try {
            LocalDate fecha = Horarios.parseFecha(reservaForm.getFecha(), "Fecha inválida.");
            LocalTime inicio = Horarios.parseHora(reservaForm.getHoraInicio(), "Hora inválida.");
            ReservaDto r = reservaService.crear(usuario.id(), reservaForm.getCanchaId(), fecha, inicio, reservaForm.getMetodoPago());
            ra.addFlashAttribute("toast", "¡Reserva " + r.codigo() + " confirmada! Pago aprobado por " + r.total() + " soles.");
            return "redirect:/mis-reservas?ok=1";
        } catch (ApiException e) {
            if (e.getStatus() == HttpStatus.CONFLICT) {
                ra.addFlashAttribute("toast", e.getMessage());
                ra.addFlashAttribute("toastTipo", "error");
                return "redirect:/canchas/" + reservaForm.getCanchaId();
            }
            model.addAttribute("pagoError", e.getMessage());
            prepararPago(model, reservaForm);
            return "pago";
        }
    }

    private void prepararPago(Model model, ReservaForm form) {
        CanchaDto cancha = canchaService.detalle(form.getCanchaId());
        LocalTime inicio = Horarios.parseHora(form.getHoraInicio(), "Hora inválida.");
        model.addAttribute("cancha", cancha);
        model.addAttribute("horaFin", Horarios.hora(inicio.plusMinutes(Horarios.DURACION_BLOQUE_MIN)));
        model.addAttribute("cotizacion", Cotizacion.de(cancha.precioHora()));
    }

    private static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    /* ---------------- Historial ---------------- */

    @GetMapping("/mis-reservas")
    public String misReservas(@AuthenticationPrincipal AuthUser usuario,
                              @RequestParam(defaultValue = "proximas") String tab, Model model) {
        LocalDate hoy = Horarios.hoy();
        List<ReservaDto> todas = reservaService.listar(usuario.id());
        Map<String, Long> conteos = new LinkedHashMap<>();
        conteos.put("todas", (long) todas.size());
        for (String c : List.of("proximas", "pasadas", "canceladas")) {
            conteos.put(c, todas.stream().filter(r -> r.categoria(hoy).equals(c)).count());
        }
        List<ReservaDto> lista = todas.stream()
                .filter(r -> "todas".equals(tab) || r.categoria(hoy).equals(tab))
                .sorted((a, b) -> "proximas".equals(tab) ? a.fecha().compareTo(b.fecha()) : b.fecha().compareTo(a.fecha()))
                .toList();
        model.addAttribute("reservas", lista);
        model.addAttribute("conteos", conteos);
        model.addAttribute("tab", tab);
        model.addAttribute("hoyFecha", hoy);
        if (!model.containsAttribute("reprogramarForm")) model.addAttribute("reprogramarForm", new ReprogramarForm());
        return "mis-reservas";
    }

    @PostMapping("/reservas/{id}/cancelar")
    public String cancelar(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id, RedirectAttributes ra) {
        try {
            reservaService.cancelar(usuario.id(), id);
            ra.addFlashAttribute("toast", "Reserva cancelada. El reembolso llegará en créditos CourtHub.");
            ra.addFlashAttribute("toastTipo", "info");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/mis-reservas";
    }

    @PostMapping("/reservas/{id}/reprogramar")
    public String reprogramar(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                              @Valid @ModelAttribute ReprogramarForm reprogramarForm, BindingResult result,
                              RedirectAttributes ra) {
        try {
            if (result.hasErrors()) throw ApiException.badRequest(result.getAllErrors().get(0).getDefaultMessage());
            LocalDate fecha = Horarios.parseFecha(reprogramarForm.getFecha(), "Selecciona una fecha válida (hoy o futura).");
            LocalTime inicio = Horarios.parseHora(reprogramarForm.getHoraInicio(), "Selecciona un bloque horario.");
            reservaService.reprogramar(usuario.id(), id, fecha, inicio);
            ra.addFlashAttribute("toast", "¡Reserva reprogramada con éxito!");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/mis-reservas";
    }
}

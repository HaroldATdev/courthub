package com.courthub.controller;

import com.courthub.dto.ClaseDto;
import com.courthub.dto.form.Formularios.ClasePagoForm;
import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.service.BoletaService;
import com.courthub.service.ClaseService;
import com.courthub.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Vista de clases deportivas: listado por deporte, inscripción (con pago si la clase tiene precio) y anulación.
 * Las clases de pago pasan por la pantalla /clases/{id}/pago; las gratuitas se inscriben directamente.
 */
@Controller
@RequiredArgsConstructor
public class ClaseViewController {

    private final ClaseService claseService;
    private final PagoService pagoService;
    private final BoletaService boletaService;

    @Value("${CULQI_PUBLIC_KEY:}")
    private String culqiPublicKeyBruta;

    private String culqiPublicKey() { return com.courthub.pasarela.Llaves.limpiar(culqiPublicKeyBruta); }

    @GetMapping("/clases")
    public String clases(@AuthenticationPrincipal AuthUser usuario,
                         @RequestParam(defaultValue = "todos") String deporte, Model model) {
        model.addAttribute("clases", claseService.listar(usuario.id(), deporte));
        model.addAttribute("deporte", deporte);
        return "clases";
    }

    /* ---------------- Pago de la inscripción ---------------- */

    @GetMapping("/clases/{id}/pago")
    public String pago(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                       @RequestParam(defaultValue = "todos") String deporte, Model model, RedirectAttributes ra) {
        ClaseDto clase = claseService.paraPago(usuario.id(), id);
        boolean pasada = java.time.LocalDate.parse(clase.fecha()).isBefore(com.courthub.util.Horarios.hoy());
        if (pasada || clase.inscrito() || clase.cuposDisponibles() <= 0 || clase.precio().signum() <= 0) {
            ra.addFlashAttribute("toast", pasada ? "Esta clase ya se realizó y no admite inscripciones."
                    : clase.inscrito() ? "Ya estás inscrito en esta clase."
                    : clase.cuposDisponibles() <= 0 ? "Lo sentimos, el cupo está lleno." : "Esta clase es gratuita: inscríbete desde el listado.");
            ra.addFlashAttribute("toastTipo", "info");
            return "redirect:/clases?deporte=" + deporte;
        }
        ClasePagoForm form = new ClasePagoForm();
        form.setDeporte(deporte);
        model.addAttribute("clasePagoForm", form);
        prepararPago(model, clase);
        return "pago-clase";
    }

    @PostMapping("/clases/{id}/inscribirse")
    public String inscribirse(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                              @ModelAttribute ClasePagoForm clasePagoForm, BindingResult result,
                              Model model, RedirectAttributes ra) {
        String deporte = clasePagoForm.getDeporte();
        try {
            ClaseDto clase = claseService.paraPago(usuario.id(), id);
            boolean cobrada = clase.precio().signum() > 0;
            if (cobrada) {
                PagoFormUtil.validar(clasePagoForm.isTerminos(), clasePagoForm.getMetodoPago(), clasePagoForm.getTokenCulqi(),
                        clasePagoForm.getNombreTarjeta(), clasePagoForm.getNumeroTarjeta(), clasePagoForm.getVencimiento(),
                        clasePagoForm.getCvv(), pagoService.pasarela().usaToken(), result);
                if (result.hasErrors()) {
                    model.addAttribute("pagoError", result.getAllErrors().get(0).getDefaultMessage());
                    prepararPago(model, clase);
                    return "pago-clase";
                }
            }
            var r = claseService.inscribirse(usuario.id(), id, new PagoService.DatosPago(
                    clasePagoForm.getMetodoPago(), clasePagoForm.getTokenCulqi(), clasePagoForm.getNumeroTarjeta()));
            ra.addFlashAttribute("toast", r.get("mensaje"));
        } catch (ApiException e) {
            if (e.getStatus() == HttpStatus.PAYMENT_REQUIRED) {
                // Pago rechazado: se vuelve a la pantalla de pago con el motivo
                model.addAttribute("pagoError", e.getMessage());
                prepararPago(model, claseService.paraPago(usuario.id(), id));
                return "pago-clase";
            }
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/clases?deporte=" + deporte;
    }

    private void prepararPago(Model model, ClaseDto clase) {
        model.addAttribute("clase", clase);
        model.addAttribute("monto", clase.precio());
        model.addAttribute("montoCentimos", clase.precio().movePointRight(2).longValue());
        model.addAttribute("culqiPublicKey", culqiPublicKey());
        model.addAttribute("usaToken", pagoService.pasarela().usaToken());
    }

    @PostMapping("/clases/{id}/abandonar")
    public String abandonar(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                            @RequestParam(defaultValue = "todos") String deporte, RedirectAttributes ra) {
        try {
            var r = claseService.abandonar(usuario.id(), id);
            ra.addFlashAttribute("toast", r.get("mensaje"));
            ra.addFlashAttribute("toastTipo", "info");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/clases?deporte=" + deporte;
    }

    /** Boleta de la inscripción del usuario a esa clase. */
    @GetMapping("/clases/{id}/boleta")
    public ResponseEntity<byte[]> boleta(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id) {
        BoletaService.Boleta b = boletaService.generarDeClase(id, usuario);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + b.nombreArchivo() + "\"")
                .body(b.pdf());
    }
}

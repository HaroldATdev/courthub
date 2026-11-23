package com.courthub.controller;

import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.service.EntrenadorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Ambiente del entrenador (/entrenador/**, solo perfil "entrenador"): resumen de sus clases e ingresos,
 * lista de alumnos de cada clase y control de asistencia.
 */
@Controller
@RequestMapping("/entrenador")
@RequiredArgsConstructor
public class EntrenadorViewController {

    private final EntrenadorService entrenador;

    @GetMapping({"", "/"})
    public String panel(@AuthenticationPrincipal AuthUser usuario, Model model) {
        model.addAttribute("p", entrenador.panel(usuario.id()));
        return "entrenador/resumen";
    }

    @GetMapping("/clases/{id}")
    public String alumnos(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id, Model model) {
        model.addAttribute("d", entrenador.detalle(usuario.id(), id));
        return "entrenador/alumnos";
    }

    @PostMapping("/clases/{id}/inscripciones/{inscripcionId}/asistencia")
    public String asistencia(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id, @PathVariable Long inscripcionId,
                             @RequestParam boolean asistio, RedirectAttributes ra) {
        try {
            entrenador.marcarAsistencia(usuario.id(), id, inscripcionId, asistio);
            ra.addFlashAttribute("toast", asistio ? "Asistencia registrada." : "Asistencia desmarcada.");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/entrenador/clases/" + id;
    }
}

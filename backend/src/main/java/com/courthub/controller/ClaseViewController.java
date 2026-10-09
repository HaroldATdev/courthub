package com.courthub.controller;

import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.service.ClaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Vista de clases deportivas: listado por deporte e inscripción/anulación mediante formularios POST. */
@Controller
@RequiredArgsConstructor
public class ClaseViewController {

    private final ClaseService claseService;

    @GetMapping("/clases")
    public String clases(@AuthenticationPrincipal AuthUser usuario,
                         @RequestParam(defaultValue = "todos") String deporte, Model model) {
        model.addAttribute("clases", claseService.listar(usuario.id(), deporte));
        model.addAttribute("deporte", deporte);
        return "clases";
    }

    @PostMapping("/clases/{id}/inscribirse")
    public String inscribirse(@AuthenticationPrincipal AuthUser usuario, @PathVariable Long id,
                              @RequestParam(defaultValue = "todos") String deporte, RedirectAttributes ra) {
        try {
            var r = claseService.inscribirse(usuario.id(), id);
            ra.addFlashAttribute("toast", r.get("mensaje"));
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/clases?deporte=" + deporte;
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
}

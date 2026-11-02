package com.courthub.controller;

import com.courthub.exception.ApiException;
import com.courthub.service.RecuperacionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/** Recuperación de contraseña: /olvide-clave (pide el correo) y /restablecer?token=... (define la clave nueva). */
@Controller
@RequiredArgsConstructor
public class RecuperacionViewController {

    private final RecuperacionService recuperacion;

    @GetMapping("/olvide-clave")
    public String formulario() {
        return "olvide-clave";
    }

    @PostMapping("/olvide-clave")
    public String solicitar(@RequestParam String identificador, HttpServletRequest request, Model model) {
        if (identificador != null && !identificador.isBlank()) {
            String base = ServletUriComponentsBuilder.fromContextPath(request).build().toUriString();
            recuperacion.solicitar(identificador, base);
        }
        // Mismo mensaje exista o no la cuenta: no se revela qué correos están registrados
        model.addAttribute("enviado", true);
        return "olvide-clave";
    }

    @GetMapping("/restablecer")
    public String restablecer(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("tokenValido", recuperacion.tokenValido(token));
        return "restablecer";
    }

    @PostMapping("/restablecer")
    public String guardar(@RequestParam String token, @RequestParam String password, @RequestParam String password2, Model model) {
        model.addAttribute("token", token);
        model.addAttribute("tokenValido", true);
        if (!password.equals(password2)) {
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "restablecer";
        }
        try {
            recuperacion.restablecer(token, password);
            return "redirect:/login?clave=1";
        } catch (ApiException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("tokenValido", recuperacion.tokenValido(token));
            return "restablecer";
        }
    }
}

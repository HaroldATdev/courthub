package com.courthub.controller;

import com.courthub.dto.AuthDtos.UsuarioDto;
import com.courthub.dto.form.Formularios.PerfilForm;
import com.courthub.exception.ApiException;
import com.courthub.model.Usuario;
import com.courthub.security.AuthUser;
import com.courthub.security.TokenCookie;
import com.courthub.service.AuthService;
import com.courthub.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * "Mi perfil": el usuario autenticado completa o corrige sus datos (DNI, nombres, apellidos, usuario,
 * teléfono, nivel, deporte favorito). El correo y la contraseña no se editan aquí: el correo identifica
 * la cuenta (y es el que entrega Google) y la clave se gestionará con autenticación de dos factores.
 */
@Controller
@RequiredArgsConstructor
public class PerfilViewController {

    private final AuthService authService;
    private final JwtService jwtService;

    @GetMapping("/perfil")
    public String perfil(@AuthenticationPrincipal AuthUser usuario, Model model) {
        Usuario u = authService.obtener(usuario.id());
        if (!model.containsAttribute("perfilForm")) model.addAttribute("perfilForm", PerfilForm.de(u));
        model.addAttribute("cuenta", UsuarioDto.de(u));
        return "perfil";
    }

    @PostMapping("/perfil")
    public String guardar(@AuthenticationPrincipal AuthUser usuario, @Valid @ModelAttribute PerfilForm perfilForm,
                          BindingResult result, Model model, HttpServletResponse response, RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("perfilError", result.getAllErrors().get(0).getDefaultMessage());
            model.addAttribute("cuenta", UsuarioDto.de(authService.obtener(usuario.id())));
            return "perfil";
        }
        try {
            Usuario u = authService.actualizarPerfil(usuario.id(), perfilForm);
            // El JWT lleva nombre, nivel y deporte (se muestran en el header): se reemite con los datos nuevos
            response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.crear(jwtService.generar(u), jwtService.getHorasExpiracion()).toString());
            ra.addFlashAttribute("toast", "Perfil actualizado correctamente.");
            return "redirect:/perfil";
        } catch (ApiException e) {
            model.addAttribute("perfilError", e.getMessage());
            model.addAttribute("cuenta", UsuarioDto.de(authService.obtener(usuario.id())));
            return "perfil";
        }
    }
}

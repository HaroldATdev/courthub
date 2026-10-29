package com.courthub.controller;

import com.courthub.dto.AuthDtos.AuthResponse;
import com.courthub.dto.AuthDtos.LoginRequest;
import com.courthub.dto.AuthDtos.RegisterRequest;
import com.courthub.dto.AuthDtos.RegisterResponse;
import com.courthub.dto.form.Formularios.LoginForm;
import com.courthub.dto.form.Formularios.RegistroForm;
import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.security.TokenCookie;
import com.courthub.service.AuthService;
import com.courthub.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Vista de Login / Registro (index.html). Al autenticar, el JWT se guarda en una cookie HttpOnly
 * y se redirige al dashboard (cliente/entrenador) o al panel ERP (administrador).
 */
@Controller
@RequiredArgsConstructor
public class AuthViewController {

    private final AuthService authService;
    private final JwtService jwtService;

    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal AuthUser usuario) {
        return "redirect:" + destino(usuario);
    }

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AuthUser usuario, @RequestParam(defaultValue = "login") String tab,
                        @RequestParam(required = false) String salir, @RequestParam(required = false) String registrado,
                        @RequestParam(required = false) String google, Model model) {
        if (usuario != null) return "redirect:" + destino(usuario);
        model.addAttribute("tab", tab);
        if (!model.containsAttribute("loginForm")) model.addAttribute("loginForm", new LoginForm());
        if (!model.containsAttribute("registroForm")) model.addAttribute("registroForm", new RegistroForm());
        if (salir != null) model.addAttribute("info", "Sesión cerrada correctamente.");
        if (registrado != null) model.addAttribute("info", AuthService.MSG_REGISTRO_OK);
        if (google != null) {
            model.addAttribute("loginError", switch (google) {
                case "pendiente" -> "Tu cuenta de Google quedó registrada en estado pendiente. " + AuthService.MSG_PENDIENTE;
                case "anulada" -> AuthService.MSG_ANULADA;
                default -> "No se pudo iniciar sesión con Google. Inténtalo de nuevo o usa el formulario.";
            });
        }
        return "index";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm loginForm, BindingResult result,
                        Model model, HttpServletResponse response) {
        model.addAttribute("tab", "login");
        model.addAttribute("registroForm", new RegistroForm());
        if (result.hasErrors()) {
            model.addAttribute("loginError", result.getAllErrors().get(0).getDefaultMessage());
            return "index";
        }
        try {
            AuthResponse auth = authService.login(new LoginRequest(loginForm.getUsuario(), loginForm.getPassword()));
            return iniciarSesion(auth, response);
        } catch (ApiException e) {
            model.addAttribute("loginError", e.getMessage());
            return "index";
        }
    }

    @PostMapping("/registro")
    public String registro(@Valid @ModelAttribute RegistroForm registroForm, BindingResult result,
                           Model model, HttpServletResponse response) {
        model.addAttribute("tab", "registro");
        model.addAttribute("loginForm", new LoginForm());
        if (!result.hasErrors() && !registroForm.getPassword().equals(registroForm.getPassword2())) {
            result.rejectValue("password2", "noCoincide", "Las contraseñas no coinciden.");
        }
        if (result.hasErrors()) {
            model.addAttribute("registroError", result.getAllErrors().get(0).getDefaultMessage());
            return "index";
        }
        try {
            // El estado inicial es "pendiente": no se inicia sesión; el administrador debe activar la cuenta.
            RegisterResponse r = authService.registrar(new RegisterRequest(
                    registroForm.getDni(), registroForm.getNombres(), registroForm.getApellidos(), registroForm.getUsuario(),
                    registroForm.getEmail(), registroForm.getPassword(), registroForm.getRol()));
            return "redirect:/login?registrado=1";
        } catch (ApiException e) {
            model.addAttribute("registroError", e.getMessage());
            return "index";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.borrar().toString());
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        return "redirect:/login?salir=1";
    }

    /** Página 403 (también recibe el forward de un POST rechazado, p. ej. token CSRF inválido). */
    @RequestMapping("/acceso-denegado")
    public ModelAndView accesoDenegado() {
        return ViewExceptionHandler.pagina(HttpStatus.FORBIDDEN, "No tienes permisos para acceder a esta sección.");
    }

    private String iniciarSesion(AuthResponse auth, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, TokenCookie.crear(auth.token(), jwtService.getHorasExpiracion()).toString());
        return "redirect:" + (AuthUser.ADMINISTRADOR.equals(auth.usuario().rol()) ? "/admin" : "/dashboard");
    }

    private static String destino(AuthUser usuario) {
        if (usuario == null) return "/login";
        return usuario.esAdmin() ? "/admin" : "/dashboard";
    }
}

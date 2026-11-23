package com.courthub.controller.api;

import com.courthub.dto.AuthDtos.*;
import com.courthub.security.AuthUser;
import com.courthub.dto.form.Formularios.PerfilForm;
import com.courthub.service.AuthService;
import com.courthub.service.DosFactoresService;
import jakarta.validation.constraints.NotBlank;
import com.courthub.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** /api/auth — registro, login (JWT) y perfil. */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthApiController {

    private final AuthService service;
    private final JwtService jwtService;
    private final DosFactoresService dosFactores;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registrar(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(req));
    }

    /**
     * Con el 2FA desactivado devuelve el JWT. Con el 2FA activo devuelve {requiere2fa, desafio, mensaje} y el JWT se
     * obtiene con POST /api/auth/2fa (desafio + código recibido por correo).
     */
    @PostMapping("/login")
    public Object login(@Valid @RequestBody LoginRequest req) {
        var u = service.validarCredenciales(req);
        if (dosFactores.habilitado()) {
            return Map.of("requiere2fa", true, "desafio", dosFactores.iniciar(u),
                    "mensaje", "Te enviamos un código de verificación a " + DosFactoresService.enmascarar(u.getEmail()) + ".");
        }
        return service.emitir(u);
    }

    public record Verificar2faRequest(@NotBlank String desafio, @NotBlank String codigo) {}

    @PostMapping("/2fa")
    public AuthResponse verificar2fa(@Valid @RequestBody Verificar2faRequest req) {
        return service.emitir(dosFactores.verificar(req.desafio(), req.codigo()));
    }

    @GetMapping("/me")
    public Map<String, UsuarioDto> perfil(@AuthenticationPrincipal AuthUser user) {
        return Map.of("usuario", service.perfil(user.id()));
    }

    /** Actualiza los datos del propio usuario y devuelve un JWT nuevo (lleva nombre, nivel y deporte). */
    @PutMapping("/me")
    public AuthResponse actualizarPerfil(@AuthenticationPrincipal AuthUser user, @Valid @RequestBody PerfilForm form) {
        var u = service.actualizarPerfil(user.id(), form);
        return new AuthResponse(jwtService.generar(u), UsuarioDto.de(u));
    }
}

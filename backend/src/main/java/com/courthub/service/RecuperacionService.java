package com.courthub.service;

import com.courthub.exception.ApiException;
import com.courthub.model.ClaveReset;
import com.courthub.model.Usuario;
import com.courthub.repository.ClaveResetRepository;
import com.courthub.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Recuperación de contraseña: se genera un token aleatorio de 256 bits, se envía por correo y en la BD solo
 * queda su hash SHA-256. El enlace caduca a los 30 minutos y es de un solo uso. Solo las cuentas activas
 * pueden restablecer (una cuenta pendiente o anulada no se "reactiva" por esta vía).
 */
@Service
@RequiredArgsConstructor
public class RecuperacionService {

    private static final int MINUTOS_VIGENCIA = 30;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarios;
    private final ClaveResetRepository resets;
    private final PasswordEncoder encoder;
    private final MailService mail;

    /** Siempre responde igual desde fuera (no revela si el correo existe): devuelve el token solo si se creó. */
    @Transactional
    public void solicitar(String identificador, String urlBase) {
        usuarios.findByUsuarioOEmail(identificador.trim()).filter(Usuario::estaActivo).ifPresent(u -> {
            resets.invalidarPendientes(u.getId());
            byte[] bytes = new byte[32];
            RANDOM.nextBytes(bytes);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            resets.save(ClaveReset.builder().usuario(u).tokenHash(hash(token))
                    .expira(OffsetDateTime.now().plusMinutes(MINUTOS_VIGENCIA)).build());
            mail.enviarRecuperacion(u.getEmail(), u.getNombre(), urlBase + "/restablecer?token=" + token);
        });
    }

    @Transactional(readOnly = true)
    public boolean tokenValido(String token) {
        return token != null && buscarVigente(token) != null;
    }

    @Transactional
    public void restablecer(String token, String nuevaClave) {
        ClaveReset r = token == null ? null : buscarVigente(token);
        if (r == null) throw ApiException.badRequest("El enlace no es válido o ya venció. Solicita uno nuevo.");
        if (nuevaClave == null || nuevaClave.length() < 6) throw ApiException.badRequest("La contraseña debe tener al menos 6 caracteres.");
        Usuario u = r.getUsuario();
        u.setPasswordHash(encoder.encode(nuevaClave));
        usuarios.save(u);
        r.setUsado(true);
        resets.save(r);
    }

    private ClaveReset buscarVigente(String token) {
        return resets.findByTokenHash(hash(token))
                .filter(r -> !r.getUsado() && r.getExpira().isAfter(OffsetDateTime.now()) && r.getUsuario().estaActivo())
                .orElse(null);
    }

    private static String hash(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

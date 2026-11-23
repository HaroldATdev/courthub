package com.courthub.service;

import com.courthub.exception.ApiException;
import com.courthub.model.Usuario;
import com.courthub.model.Verificacion2fa;
import com.courthub.repository.Verificacion2faRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Verificación en dos pasos por correo. Tras validar usuario y clave se envía un código de 6 dígitos al correo de la
 * cuenta; solo con ese código se emite la sesión. Así el correo queda comprobado en cada acceso y las cuentas de
 * cliente no necesitan aprobación del administrador.
 *
 * Seguridad: el código y el desafío se guardan solo como hash; vence a los 10 minutos, es de un solo uso y admite 5
 * intentos (al 5.º fallo se invalida). El "desafío" es un token aleatorio de 256 bits que viaja en el formulario y
 * liga el código con el intento de acceso concreto.
 *
 * Solo se activa si hay un proveedor de correo real (BREVO_API_KEY o MAIL_HOST) y TWOFA_ENABLED no es "false":
 * sin correo no habría forma de entregar el código y nadie podría entrar.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DosFactoresService {

    public static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_VIGENCIA = 10;
    private static final int SEGUNDOS_ENTRE_ENVIOS = 60;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Verificacion2faRepository verificaciones;
    private final MailService mail;

    @Value("${TWOFA_ENABLED:true}")
    private boolean activadoPorConfig;

    public boolean habilitado() {
        return activadoPorConfig && mail.configurado();
    }

    /** Crea un código, lo envía por correo y devuelve el desafío que debe acompañar a la verificación. */
    @Transactional
    public String iniciar(Usuario u) {
        verificaciones.invalidarPendientes(u.getId());
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String desafio = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String codigo = String.format("%06d", RANDOM.nextInt(1_000_000));
        verificaciones.save(Verificacion2fa.builder().usuario(u)
                .desafioHash(hash(desafio)).codigoHash(hash(desafio + ":" + codigo))
                .expira(OffsetDateTime.now().plusMinutes(MINUTOS_VIGENCIA)).build());
        if (!mail.enviarCodigo2fa(u.getEmail(), u.getNombre(), codigo)) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No pudimos enviar el código de verificación a tu correo. Inténtalo de nuevo en unos minutos.");
        }
        return desafio;
    }

    /**
     * Comprueba el código. Devuelve el usuario si es correcto. Los intentos fallidos se guardan aunque se lance la
     * excepción (noRollbackFor), si no el límite de 5 intentos no serviría.
     */
    @Transactional(noRollbackFor = ApiException.class)
    public Usuario verificar(String desafio, String codigo) {
        Verificacion2fa v = desafio == null || desafio.isBlank() ? null : verificaciones.findByDesafioHash(hash(desafio)).orElse(null);
        if (v == null || v.getUsado() || v.getExpira().isBefore(OffsetDateTime.now())) {
            throw ApiException.unauthorized("El código venció o ya no es válido. Inicia sesión de nuevo.");
        }
        String limpio = codigo == null ? "" : codigo.replaceAll("\\D", "");
        boolean correcto = MessageDigest.isEqual(hash(desafio + ":" + limpio).getBytes(StandardCharsets.UTF_8),
                v.getCodigoHash().getBytes(StandardCharsets.UTF_8));
        if (!correcto) {
            v.setIntentos((short) (v.getIntentos() + 1));
            boolean agotado = v.getIntentos() >= MAX_INTENTOS;
            if (agotado) v.setUsado(true);
            verificaciones.save(v);
            throw ApiException.unauthorized(agotado
                    ? "Demasiados intentos fallidos. Inicia sesión de nuevo para recibir otro código."
                    : "Código incorrecto. Te quedan " + (MAX_INTENTOS - v.getIntentos()) + " intento(s).");
        }
        v.setUsado(true);
        verificaciones.save(v);
        Usuario u = v.getUsuario();
        if (!u.estaActivo()) throw ApiException.forbidden("Tu cuenta no está activa.");
        return u;
    }

    /** Reenvía un código nuevo (máximo uno por minuto) para el mismo intento de acceso; devuelve el desafío nuevo. */
    @Transactional(noRollbackFor = ApiException.class)
    public String reenviar(String desafio) {
        Verificacion2fa v = desafio == null || desafio.isBlank() ? null : verificaciones.findByDesafioHash(hash(desafio)).orElse(null);
        if (v == null) throw ApiException.unauthorized("La verificación venció. Inicia sesión de nuevo.");
        long segundos = Duration.between(v.getFechaCreacion(), OffsetDateTime.now()).getSeconds();
        if (segundos < SEGUNDOS_ENTRE_ENVIOS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS,
                    "Espera " + (SEGUNDOS_ENTRE_ENVIOS - segundos) + " segundos antes de pedir otro código.");
        }
        return iniciar(v.getUsuario());
    }

    /** "ana@courthub.com" → "a**@courthub.com" (se muestra en la pantalla de verificación). */
    public static String enmascarar(String correo) {
        int i = correo.indexOf('@');
        if (i <= 1) return correo;
        return correo.charAt(0) + "*".repeat(Math.min(i - 1, 6)) + correo.substring(i);
    }

    private static String hash(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.courthub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Envío de correos de la aplicación. Tres modos, en este orden de prioridad:
 *  1. API HTTPS de Brevo (si se define BREVO_API_KEY). Usa el puerto 443, que los hosting gratuitos no bloquean;
 *     es la opción para Render, donde el SMTP (puertos 25/465/587) puede estar bloqueado.
 *  2. SMTP (si se define MAIL_HOST).
 *  3. Sin proveedor: el mensaje solo se escribe en el log del servidor (modo desarrollo).
 */
@Slf4j
@Service
public class MailService {

    private final ObjectProvider<JavaMailSender> smtp;
    private final String remitente;
    private final String brevoKey;
    private final String brevoUrl;
    private final ObjectMapper json = new ObjectMapper();

    public MailService(ObjectProvider<JavaMailSender> smtp,
                       @Value("${MAIL_FROM:${MAIL_USERNAME:no-reply@courthub.local}}") String remitente,
                       @Value("${BREVO_API_KEY:}") String brevoKey,
                       @Value("${BREVO_API_URL:https://api.brevo.com/v3/smtp/email}") String brevoUrl) {
        this.smtp = smtp;
        this.remitente = remitente;
        this.brevoKey = brevoKey;
        this.brevoUrl = brevoUrl;
    }

    /** true si hay un proveedor real (Brevo o SMTP) para entregar correos. */
    public boolean configurado() {
        return !brevoKey.isBlank() || smtp.getIfAvailable() != null;
    }

    /** Nombre del proveedor activo, para el log de arranque y el README. */
    public String proveedor() {
        return !brevoKey.isBlank() ? "brevo-api" : smtp.getIfAvailable() != null ? "smtp" : "ninguno (log)";
    }

    /** Envío síncrono: devuelve false si el proveedor falló (el 2FA necesita saberlo). Sin proveedor, escribe en el log. */
    public boolean enviar(String destino, String asunto, String texto) {
        try {
            if (!brevoKey.isBlank()) return enviarBrevo(destino, asunto, texto);
            JavaMailSender s = smtp.getIfAvailable();
            if (s == null) {
                log.info("[Correo no configurado] Para {} · {}\n{}", destino, asunto, texto);
                return false;
            }
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(remitente);
            m.setTo(destino);
            m.setSubject(asunto);
            m.setText(texto);
            s.send(m);
            log.info("[Correo] SMTP: enviado a {} ({})", destino, asunto);
            return true;
        } catch (Exception e) {
            log.error("[Correo] No se pudo enviar a {}: {}", destino, e.getMessage());
            return false;
        }
    }

    private boolean enviarBrevo(String destino, String asunto, String texto) throws Exception {
        Map<String, Object> cuerpo = Map.of(
                "sender", Map.of("name", "CourtHub", "email", remitente),
                "to", List.of(Map.of("email", destino)),
                "subject", asunto,
                "textContent", texto);
        byte[] bytes = json.writeValueAsBytes(cuerpo);   // con Content-Length explícito (sin transfer-encoding chunked)
        String respuesta = com.courthub.util.Http.builder().build().post().uri(brevoUrl)
                .header("api-key", brevoKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentLength(bytes.length)
                .body(bytes)
                .exchange((req, res) -> {
                    String t = new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    return (res.getStatusCode().is2xxSuccessful() ? "OK|" : "ERR " + res.getStatusCode().value() + "|") + t;
                });
        if (respuesta.startsWith("OK|")) {
            JsonNode n = json.readTree(respuesta.substring(3));
            log.info("[Correo] Brevo: enviado a {} ({}) messageId={}", destino, asunto, n.path("messageId").asText("-"));
            return true;
        }
        log.error("[Correo] Brevo rechazó el envío a {}: {}", destino, respuesta);
        return false;
    }

    @Async
    public void enviarRecuperacion(String destino, String nombre, String enlace) {
        boolean ok = enviar(destino, "CourtHub: restablece tu contraseña",
                "Hola " + nombre + ",\n\nRecibimos una solicitud para restablecer la contraseña de tu cuenta de CourtHub.\n"
                        + "Abre este enlace (vale 30 minutos y solo se puede usar una vez):\n\n" + enlace
                        + "\n\nSi no fuiste tú, ignora este mensaje: tu contraseña no cambiará.\n\n— CourtHub");
        if (!ok && !configurado()) log.info("[Correo no configurado] Enlace de recuperación para {}: {}", destino, enlace);
    }

    /** Código de verificación en dos pasos. Síncrono: si no se pudo entregar, el login debe fallar con un aviso claro. */
    public boolean enviarCodigo2fa(String destino, String nombre, String codigo) {
        return enviar(destino, "CourtHub: tu código de verificación " + codigo,
                "Hola " + nombre + ",\n\nTu código de verificación de CourtHub es:\n\n    " + codigo
                        + "\n\nVale 10 minutos y solo se puede usar una vez. Si no intentaste iniciar sesión, cambia tu contraseña.\n\n— CourtHub");
    }
}

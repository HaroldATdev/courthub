package com.courthub.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/** Envío de correos de la aplicación (recuperación de contraseña). Asíncrono para no bloquear la petición. */
@Slf4j
@Service
public class MailService {

    private final ObjectProvider<JavaMailSender> sender;
    private final String remitente;

    public MailService(ObjectProvider<JavaMailSender> sender,
                       @Value("${MAIL_FROM:${MAIL_USERNAME:no-reply@courthub.local}}") String remitente) {
        this.sender = sender;
        this.remitente = remitente;
    }

    public boolean configurado() {
        return sender.getIfAvailable() != null;
    }

    @Async
    public void enviarRecuperacion(String destino, String nombre, String enlace) {
        JavaMailSender s = sender.getIfAvailable();
        if (s == null) {
            // Modo desarrollo: sin SMTP el enlace se muestra solo en el log del servidor
            log.info("[Correo no configurado] Enlace de recuperación para {}: {}", destino, enlace);
            return;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(remitente);
            m.setTo(destino);
            m.setSubject("CourtHub: restablece tu contraseña");
            m.setText("Hola " + nombre + ",\n\nRecibimos una solicitud para restablecer la contraseña de tu cuenta de CourtHub.\n"
                    + "Abre este enlace (vale 30 minutos y solo se puede usar una vez):\n\n" + enlace
                    + "\n\nSi no fuiste tú, ignora este mensaje: tu contraseña no cambiará.\n\n— CourtHub");
            s.send(m);
            log.info("[Correo] Recuperación enviada a {}", destino);
        } catch (Exception e) {
            log.error("[Correo] No se pudo enviar a {}: {}", destino, e.getMessage());
        }
    }
}

package com.courthub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Envío de correo por SMTP, solo si se define MAIL_HOST (p. ej. smtp.gmail.com con una "contraseña de
 * aplicación" de Google, o el SMTP de Brevo/Resend/Mailtrap). Sin MAIL_HOST la aplicación arranca igual y
 * el enlace de recuperación de contraseña se escribe en el log del servidor (modo desarrollo).
 */
@Configuration
@ConditionalOnExpression("'${MAIL_HOST:}' != ''")
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(@Value("${MAIL_HOST}") String host,
                                         @Value("${MAIL_PORT:587}") int port,
                                         @Value("${MAIL_USERNAME:}") String usuario,
                                         @Value("${MAIL_PASSWORD:}") String clave) {
        JavaMailSenderImpl s = new JavaMailSenderImpl();
        s.setHost(host);
        s.setPort(port);
        s.setUsername(usuario);
        s.setPassword(clave);
        s.setDefaultEncoding("UTF-8");
        Properties p = s.getJavaMailProperties();
        p.put("mail.smtp.auth", "true");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.connectiontimeout", "8000");
        p.put("mail.smtp.timeout", "8000");
        p.put("mail.smtp.writetimeout", "8000");
        return s;
    }
}

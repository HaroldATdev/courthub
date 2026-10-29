package com.courthub.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

/**
 * Registro del proveedor Google para "Continuar con Google".
 * Solo se activa si existen las variables de entorno GOOGLE_CLIENT_ID y GOOGLE_CLIENT_SECRET
 * (credenciales OAuth 2.0 creadas en Google Cloud Console). Sin ellas, la aplicación arranca
 * igual y el botón de Google muestra un aviso.
 */
@Configuration
@ConditionalOnExpression("'${GOOGLE_CLIENT_ID:}' != '' and '${GOOGLE_CLIENT_SECRET:}' != ''")
public class GoogleOAuthConfig {

    public static final String REGISTRATION_ID = "google";

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(@Value("${GOOGLE_CLIENT_ID}") String clientId,
                                                                     @Value("${GOOGLE_CLIENT_SECRET}") String clientSecret) {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder(REGISTRATION_ID)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid", "profile", "email")
                // URI de retorno que debe registrarse en Google Cloud Console:
                //   http://localhost:8080/login/oauth2/code/google   (y la URL pública en producción)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}

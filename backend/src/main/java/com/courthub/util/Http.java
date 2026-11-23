package com.courthub.util;

import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/** Cliente HTTP para las llamadas a servicios externos (pasarela de pago, API de correo): HTTP/1.1 y con tiempos límite. */
public final class Http {

    private Http() {}

    public static RestClient.Builder builder() {
        HttpClient cliente = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory fabrica = new JdkClientHttpRequestFactory(cliente);
        fabrica.setReadTimeout(Duration.ofSeconds(12));
        return RestClient.builder().requestFactory(fabrica);
    }
}

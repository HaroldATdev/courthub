package com.courthub.pasarela;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Cobro con Culqi. Se activa al definir CULQI_SECRET_KEY (llave privada sk_test_... del panel de integración).
 *
 * Flujo: el navegador abre Culqi Checkout con la llave pública (pk_test_...), el cliente escribe su tarjeta en la
 * ventana de Culqi y el navegador recibe un token (tkn_...). El servidor solo recibe ese token y crea el cargo
 * (POST /v2/charges) con la llave privada: el número de tarjeta nunca pasa por CourtHub.
 * El monto se vuelve a calcular en el servidor; nunca se confía en el que envía el navegador.
 */
@Slf4j
@Component
@ConditionalOnExpression("'${CULQI_SECRET_KEY:}' != ''")
public class CulqiPasarela implements PasarelaPago {

    private final RestClient http;
    private final ObjectMapper json = new ObjectMapper();

    public CulqiPasarela(@Value("${CULQI_SECRET_KEY}") String llavePrivada,
                         @Value("${CULQI_API_URL:https://api.culqi.com/v2}") String apiUrl) {
        this.http = com.courthub.util.Http.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + llavePrivada)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override public String nombre() { return "culqi"; }

    @Override public boolean usaToken() { return true; }

    @Override
    public Resultado cobrar(Cobro c) {
        if (c.token() == null || c.token().isBlank()) {
            return Resultado.rechazado("No se recibió el token de la tarjeta. Vuelve a intentar el pago.");
        }
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("amount", c.monto().movePointRight(2).setScale(0, java.math.RoundingMode.HALF_UP).intValueExact()); // céntimos
        cuerpo.put("currency_code", "PEN");
        cuerpo.put("email", c.email());
        cuerpo.put("source_id", c.token());
        cuerpo.put("description", recortar(c.descripcion(), 80));
        try {
            byte[] bytes = json.writeValueAsBytes(cuerpo);   // con Content-Length explícito (sin transfer-encoding chunked)
            String respuesta = http.post().uri("/charges").contentLength(bytes.length).body(bytes)
                    .exchange((req, res) -> {
                        String texto = new String(res.getBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                        return (res.getStatusCode().is2xxSuccessful() ? "OK|" : "ERR|") + texto;
                    });
            boolean exito = respuesta.startsWith("OK|");
            JsonNode n = json.readTree(respuesta.substring(respuesta.indexOf('|') + 1));   // "OK|{...}" o "ERR|{...}"
            if (exito && n.hasNonNull("id")) {
                String ultimos4 = n.path("source").path("last_four").asText(null);
                return Resultado.ok(n.get("id").asText(), ultimos4);
            }
            String motivo = n.path("user_message").asText(n.path("merchant_message").asText("El pago fue rechazado."));
            log.warn("[Culqi] Cargo NO aprobado. Respuesta de Culqi: {}", recortar(respuesta, 600));
            return Resultado.rechazado(motivo);
        } catch (Exception e) {
            log.error("[Culqi] Error al llamar a la pasarela ({}): {}", e.getClass().getSimpleName(), e.getMessage());
            return Resultado.rechazado("No se pudo procesar el pago en este momento. Inténtalo de nuevo en unos minutos.");
        }
    }

    private static String recortar(String s, int max) {
        String t = s == null ? "CourtHub" : s;
        return t.length() <= max ? t : t.substring(0, max);
    }
}

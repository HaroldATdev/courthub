package com.courthub.pasarela;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Pasarela simulada (modo por defecto, sin llaves): no se envía nada a ningún proveedor ni se mueve dinero.
 * Imita el comportamiento del sandbox de Culqi con las mismas tarjetas de prueba: las de la tabla de errores
 * se rechazan con su motivo y cualquier otra se aprueba.
 */
@Component
@ConditionalOnExpression("'${CULQI_SECRET_KEY:}' == ''")
public class SimuladaPasarela implements PasarelaPago {

    /** Tarjetas de prueba de Culqi que deben fallar → motivo mostrado. */
    private static final Map<String, String> RECHAZADAS = Map.of(
            "4000020000000000", "Tarjeta robada (tarjeta de prueba de rechazo).",
            "4000030000000009", "Tarjeta perdida (tarjeta de prueba de rechazo).",
            "4000040000000008", "Fondos insuficientes (tarjeta de prueba de rechazo).",
            "5400000000000005", "Contacta al banco emisor (tarjeta de prueba de rechazo).",
            "5400020000000003", "CVV incorrecto (tarjeta de prueba de rechazo).",
            "4111110000000013", "Operación denegada por el banco (tarjeta de prueba de rechazo).");

    @Override public String nombre() { return "simulado"; }

    @Override public boolean usaToken() { return false; }

    @Override
    public Resultado cobrar(Cobro c) {
        String numero = c.numeroTarjeta() == null ? "" : c.numeroTarjeta().replaceAll("\\D", "");
        if (RECHAZADAS.containsKey(numero)) return Resultado.rechazado(RECHAZADAS.get(numero));
        String ultimos4 = numero.length() >= 4 ? numero.substring(numero.length() - 4) : null;
        return Resultado.ok(null, ultimos4);
    }
}

package com.courthub.pasarela;

import java.math.BigDecimal;

/**
 * Pasarela de pagos. Hay dos implementaciones y se elige por configuración:
 *  - {@link SimuladaPasarela}: sin llaves, aprueba o rechaza según tarjetas de prueba (no mueve dinero).
 *  - {@link CulqiPasarela}: si se define CULQI_SECRET_KEY, cobra en Culqi (con llaves de prueba, en su sandbox).
 */
public interface PasarelaPago {

    /** Nombre guardado en pagos.proveedor. */
    String nombre();

    /** true si el cliente paga con un token generado en el navegador (Culqi): el número de tarjeta nunca llega al servidor. */
    boolean usaToken();

    Resultado cobrar(Cobro cobro);

    /**
     * @param token         token de tarjeta generado en el navegador (solo pasarelas que lo usan)
     * @param numeroTarjeta número escrito en el formulario (solo la pasarela simulada; nunca se guarda)
     */
    record Cobro(BigDecimal monto, String descripcion, String email, String metodo, String token, String numeroTarjeta) {}

    record Resultado(boolean aprobado, String referencia, String ultimos4, String mensaje) {
        public static Resultado ok(String referencia, String ultimos4) { return new Resultado(true, referencia, ultimos4, "Pago aprobado."); }
        public static Resultado rechazado(String mensaje) { return new Resultado(false, null, null, mensaje); }
    }
}

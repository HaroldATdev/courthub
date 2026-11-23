package com.courthub.controller;

import org.springframework.validation.BindingResult;

/** Validación común del formulario de pago (reservas y clases). */
final class PagoFormUtil {

    private PagoFormUtil() {}

    /**
     * Con Culqi la tarjeta se escribe en la ventana de Culqi y solo llega un token; con la pasarela simulada se
     * valida el formato de los campos de la tarjeta. Los errores se registran en el BindingResult del formulario.
     */
    static void validar(boolean terminos, String metodo, String token, String nombre, String numeroTarjeta,
                        String vencimiento, String cvv, boolean usaToken, BindingResult result) {
        if (!terminos) {
            result.rejectValue("terminos", "terminos", "Debes aceptar los términos y condiciones para continuar.");
            return;
        }
        if (usaToken) {
            if (token == null || token.isBlank()) {
                result.rejectValue("tokenCulqi", "token", "Completa los datos de la tarjeta en la ventana de pago de Culqi.");
            }
            return;
        }
        if (!"Tarjeta de Crédito".equals(metodo)) return;
        String numero = digitos(numeroTarjeta);
        String venc = digitos(vencimiento);
        String c = digitos(cvv);
        if (nombre == null || nombre.isBlank()) {
            result.rejectValue("nombreTarjeta", "tarjeta", "Ingresa el nombre que aparece en la tarjeta.");
        } else if (numero.length() != 16) {
            result.rejectValue("numeroTarjeta", "tarjeta", "El número de tarjeta debe tener 16 dígitos.");
        } else if (venc.length() != 4 || Integer.parseInt(venc.substring(0, 2)) < 1 || Integer.parseInt(venc.substring(0, 2)) > 12) {
            result.rejectValue("vencimiento", "tarjeta", "Ingresa una fecha de vencimiento válida (MM / AA).");
        } else if (c.length() < 3) {
            result.rejectValue("cvv", "tarjeta", "El CVV debe tener 3 o 4 dígitos.");
        }
    }

    static String digitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }
}

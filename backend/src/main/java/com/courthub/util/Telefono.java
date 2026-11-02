package com.courthub.util;

import com.courthub.exception.ApiException;

/**
 * Celulares de Perú: 9 dígitos que empiezan en 9, con código de país +51.
 * Se aceptan entradas como "987654321", "+51 987 654 321" o "51987654321"
 * y siempre se guardan como "+51 987 654 321".
 */
public final class Telefono {

    private Telefono() {}

    public static final String MENSAJE_INVALIDO = "Ingresa un celular peruano válido: 9 dígitos que empiezan en 9 (ej. 987 654 321).";

    public static String normalizar(String entrada) {
        if (entrada == null || entrada.isBlank()) return "";
        String digitos = entrada.replaceAll("\\D", "");
        if (digitos.length() == 11 && digitos.startsWith("51")) digitos = digitos.substring(2);
        if (digitos.length() != 9 || digitos.charAt(0) != '9') throw ApiException.badRequest(MENSAJE_INVALIDO);
        return "+51 " + digitos.substring(0, 3) + " " + digitos.substring(3, 6) + " " + digitos.substring(6);
    }
}

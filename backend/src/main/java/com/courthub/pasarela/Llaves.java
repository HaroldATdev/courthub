package com.courthub.pasarela;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Las llaves de Culqi se pegan a mano en variables de entorno y es fácil arrastrar un carácter de más
 * (una coma al copiarlas de un JSON, comillas, espacios o saltos de línea). Aquí se limpian y se valida su forma.
 */
@Slf4j
public final class Llaves {

    private static final Pattern FORMA = Pattern.compile("^(pk|sk)_(test|live)_[A-Za-z0-9]{16}$");

    private Llaves() {}

    /** Quita espacios, comillas, comas y puntos y coma de los bordes: "  \"sk_test_abc,\" " -> "sk_test_abc". */
    public static String limpiar(String llave) {
        if (llave == null) return "";
        String s = llave.strip();
        while (!s.isEmpty() && ",;\"' \t".indexOf(s.charAt(s.length() - 1)) >= 0) s = s.substring(0, s.length() - 1);
        while (!s.isEmpty() && ",;\"' \t".indexOf(s.charAt(0)) >= 0) s = s.substring(1);
        return s;
    }

    /** Registra en el log de arranque con qué tipo de llave se está trabajando, sin mostrar su valor. */
    public static void avisarEstado(String nombre, String original, String limpia) {
        if (!original.equals(limpia)) {
            log.warn("[Culqi] {} traía caracteres de más (espacios, comillas o coma al final); se limpiaron. Corrige la variable de entorno.", nombre);
        }
        if (!FORMA.matcher(limpia).matches()) {
            log.warn("[Culqi] {} no tiene la forma esperada (pk_/sk_ + test/live + 16 caracteres). Revísala en el panel de Culqi.", nombre);
        }
        if (limpia.contains("_live_")) {
            log.warn("[Culqi] {} es una llave de PRODUCCIÓN (_live_): los pagos serían reales. Para el proyecto usa llaves de prueba (_test_).", nombre);
        } else if (limpia.contains("_test_")) {
            log.info("[Culqi] {}: llave de PRUEBAS (sandbox, no se cobra dinero real)", nombre);
        }
    }
}

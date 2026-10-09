package com.courthub.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

/**
 * Utilidades de formato para las vistas Thymeleaf. Se usa como bean: ${@fmt.pen(total)}.
 */
@Component("fmt")
public class Fmt {

    public static final Locale ES_PE = Locale.forLanguageTag("es-PE");

    public static final Map<String, String> DEPORTES = Map.of(
            "todos", "Todos", "futbol", "Fútbol", "tenis", "Tenis", "squash", "Squash", "padel", "Pádel");

    private static final DecimalFormat SOLES = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));

    /** S/ 1,234.50 */
    public String pen(Number n) {
        BigDecimal v = n == null ? BigDecimal.ZERO : new BigDecimal(n.toString()).setScale(2, RoundingMode.HALF_UP);
        return "S/ " + SOLES.format(v);
    }

    /** 4.9 */
    public String dec1(Number n) {
        return n == null ? "0.0" : new BigDecimal(n.toString()).setScale(1, RoundingMode.HALF_UP).toPlainString();
    }

    /** Fecha ISO (yyyy-mm-dd) con un patrón en español: "Lunes 13 de oct." */
    public String fecha(String iso, String patron) {
        if (iso == null || iso.isBlank()) return "—";
        return cap(LocalDate.parse(iso.substring(0, 10)).format(DateTimeFormatter.ofPattern(patron, ES_PE)));
    }

    public String fecha(LocalDate d, String patron) {
        return d == null ? "—" : cap(d.format(DateTimeFormatter.ofPattern(patron, ES_PE)));
    }

    /** "Lunes 13 de octubre de 2026" */
    public String fechaLarga(String iso) {
        return fecha(iso, "EEEE d 'de' MMMM 'de' yyyy");
    }

    /** "Lunes 13 de octubre" */
    public String fechaMedia(String iso) {
        return fecha(iso, "EEEE d 'de' MMMM");
    }

    /** Fecha y hora de un ISO-8601 con zona (OffsetDateTime): "13 oct. 2026, 14:05" */
    public String fechaHora(String iso) {
        if (iso == null || iso.isBlank()) return "—";
        try {
            return OffsetDateTime.parse(iso).atZoneSameInstant(Horarios.LIMA)
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", ES_PE));
        } catch (Exception e) {
            return iso;
        }
    }

    /** "2026-10" → "Oct 26" */
    public String mes(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        return cap(ym.format(DateTimeFormatter.ofPattern("MMM yy", ES_PE))).replace(".", "");
    }

    public String deporte(String clave) {
        return DEPORTES.getOrDefault(clave, cap(clave));
    }

    public String cap(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    /** Porcentaje entero de a sobre b (0-100). */
    public int pct(Number a, Number b) {
        if (a == null || b == null || b.doubleValue() == 0) return 0;
        return (int) Math.min(100, Math.round(a.doubleValue() * 100 / b.doubleValue()));
    }
}

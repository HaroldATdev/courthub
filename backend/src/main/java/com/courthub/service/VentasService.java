package com.courthub.service;

import com.courthub.model.Pago;
import com.courthub.repository.PagoRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Reporte de ventas por mes y por año, calculado sobre la tabla "pagos".
 * Una venta es un pago "aprobado"; los pagos "reembolsado" (reservas canceladas) se informan aparte
 * y no suman a las ventas.
 */
@Service
@RequiredArgsConstructor
public class VentasService {

    private final PagoRepository pagos;

    /** Totales de un periodo (un mes o un año). */
    public record Periodo(int anio, int mes, long operaciones, BigDecimal subtotal, BigDecimal cargo, BigDecimal total) {
        public BigDecimal ticket() {
            return operaciones == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(operaciones), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /** Una venta del detalle (para la tabla y el CSV). */
    public record Venta(Long pagoId, String boleta, String tipo, String concepto, String operacion, ZonedDateTime fecha, String cliente,
                        String metodo, BigDecimal subtotal, BigDecimal cargo, BigDecimal total, String estado) {}

    public record Reporte(int anio, int mes, List<Integer> anios, List<Periodo> porMes, List<Periodo> porAnio,
                          Periodo periodo, BigDecimal reembolsado, long reembolsos, List<Venta> detalle) {}

    /**
     * @param anio año a consultar (null = el actual)
     * @param mes  mes 1-12 para filtrar el detalle y los indicadores (0 o null = todo el año)
     */
    @Transactional(readOnly = true)
    public Reporte reporte(Integer anio, Integer mes) {
        List<Pago> todos = pagos.findAllByOrderByFechaPagoDesc();
        int anioActual = Horarios.hoy().getYear();
        int a = anio == null || anio < 2000 || anio > 2100 ? anioActual : anio;
        int m = mes == null || mes < 1 || mes > 12 ? 0 : mes;

        TreeSet<Integer> anios = new TreeSet<>();
        anios.add(anioActual);
        todos.forEach(p -> anios.add(fecha(p).getYear()));

        List<Periodo> porMes = new ArrayList<>();
        for (int i = 1; i <= 12; i++) porMes.add(sumar(todos, a, i));

        List<Periodo> porAnio = new ArrayList<>();
        for (int y : anios.descendingSet()) porAnio.add(sumar(todos, y, 0));

        Periodo periodo = sumar(todos, a, m);

        List<Pago> reemb = todos.stream()
                .filter(p -> Pago.REEMBOLSADO.equals(p.getEstado()) && enPeriodo(p, a, m)).toList();
        BigDecimal reembolsado = reemb.stream().map(Pago::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Venta> detalle = todos.stream()
                .filter(p -> Pago.APROBADO.equals(p.getEstado()) && enPeriodo(p, a, m))
                .map(p -> new Venta(p.getId(), BoletaService.SERIE + "-" + String.format("%08d", p.getId()),
                        p.esDeReserva() ? "Reserva" : "Clase",
                        p.esDeReserva() ? p.getReserva().getCodigo() + " · " + p.getReserva().getCancha().getNombre()
                                        : p.getInscripcion().getClase().getNombre(),
                        p.getOperacion(), fecha(p),
                        (p.getUsuario().getNombre() + " " + p.getUsuario().getApellido()).trim(),
                        p.getMetodo(), p.getSubtotal(), p.getCargoServicio(), p.getMonto(), p.getEstado()))
                .toList();

        return new Reporte(a, m, new ArrayList<>(anios.descendingSet()), porMes, porAnio, periodo,
                reembolsado, reemb.size(), detalle);
    }

    private static Periodo sumar(List<Pago> todos, int anio, int mes) {
        long n = 0;
        BigDecimal sub = BigDecimal.ZERO, cargo = BigDecimal.ZERO, total = BigDecimal.ZERO;
        for (Pago p : todos) {
            if (!Pago.APROBADO.equals(p.getEstado()) || !enPeriodo(p, anio, mes)) continue;
            n++;
            sub = sub.add(p.getSubtotal());
            cargo = cargo.add(p.getCargoServicio());
            total = total.add(p.getMonto());
        }
        return new Periodo(anio, mes, n, sub, cargo, total);
    }

    /** mes = 0 significa todo el año. */
    private static boolean enPeriodo(Pago p, int anio, int mes) {
        ZonedDateTime f = fecha(p);
        return f.getYear() == anio && (mes == 0 || f.getMonthValue() == mes);
    }

    private static ZonedDateTime fecha(Pago p) {
        return p.getFechaPago().atZoneSameInstant(Horarios.LIMA);
    }
}

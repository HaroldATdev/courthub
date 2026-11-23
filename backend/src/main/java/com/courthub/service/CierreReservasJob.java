package com.courthub.service;

import com.courthub.repository.ReservaRepository;
import com.courthub.util.Horarios;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cierra el ciclo de la reserva: una reserva confirmada pasa sola a "completada" cuando termina su bloque.
 * Corre al arrancar y luego cada 5 minutos (el administrador también puede completarla a mano).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CierreReservasJob {

    private final ReservaRepository reservas;

    @Scheduled(initialDelay = 15_000, fixedDelay = 300_000)
    @Transactional
    public void completarVencidas() {
        int n = reservas.completarVencidas(Horarios.hoy(), Horarios.ahora());
        if (n > 0) log.info("[Reservas] {} reserva(s) confirmada(s) pasaron a completada", n);
    }
}

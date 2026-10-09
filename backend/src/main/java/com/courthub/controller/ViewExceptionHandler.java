package com.courthub.controller;

import com.courthub.exception.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/** Errores en las vistas MVC: se renderiza la plantilla error.html con el código HTTP adecuado. */
@Slf4j
@ControllerAdvice(assignableTypes = {
        AuthViewController.class, CanchaViewController.class, ReservaViewController.class,
        ClaseViewController.class, AdminViewController.class})
public class ViewExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ModelAndView apiException(ApiException e) {
        return pagina(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView generico(Exception e) {
        log.error("[Web] Error interno", e);
        return pagina(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Inténtalo de nuevo.");
    }

    static ModelAndView pagina(HttpStatus status, String mensaje) {
        ModelAndView mv = new ModelAndView("error");
        mv.setStatus(status);
        mv.addObject("status", status.value());
        mv.addObject("error", status.getReasonPhrase());
        mv.addObject("mensaje", mensaje);
        return mv;
    }
}

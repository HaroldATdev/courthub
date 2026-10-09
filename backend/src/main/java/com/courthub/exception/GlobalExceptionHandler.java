package com.courthub.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Manejo de errores de la API REST (controladores de com.courthub.controller.api):
 * toda excepción se convierte en JSON uniforme { "error": "mensaje" } con el código HTTP adecuado.
 * Las vistas MVC usan su propio manejador (ViewExceptionHandler).
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.courthub.controller.api")
public class GlobalExceptionHandler {

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(Map.of("error", mensaje));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> apiException(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacion(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(f -> f.getDefaultMessage()).orElse("Datos inválidos.");
        return error(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> cuerpoInvalido(Exception e) {
        return error(HttpStatus.BAD_REQUEST, "Datos de la solicitud inválidos.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException e) {
        log.warn("[API] Violación de integridad: {}", e.getMostSpecificCause().getMessage());
        return error(HttpStatus.CONFLICT, "Conflicto de datos: el registro ya existe o viola una restricción.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccessDeniedException e) {
        return error(HttpStatus.FORBIDDEN, "No tienes permisos para esta acción.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Ruta no encontrada.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> generico(Exception e) {
        log.error("[API] Error interno", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor.");
    }
}

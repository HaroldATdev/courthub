package com.courthub.security;

/**
 * Usuario autenticado (contenido del JWT). Es el "principal" de Spring Security:
 * se inyecta en los controladores con @AuthenticationPrincipal y se expone a las vistas como ${usuario}.
 */
public record AuthUser(Long id, String email, String rol, String nombre, String apellido, String nivel, String deporte) {

    public static final String CLIENTE = "cliente";
    public static final String ENTRENADOR = "entrenador";
    public static final String ADMINISTRADOR = "administrador";

    public boolean esAdmin() { return ADMINISTRADOR.equals(rol); }

    public String nombreCompleto() { return (nombre + " " + (apellido == null ? "" : apellido)).trim(); }

    public String iniciales() {
        String n = nombre == null || nombre.isEmpty() ? "U" : nombre.substring(0, 1);
        String a = apellido == null || apellido.isEmpty() ? "" : apellido.substring(0, 1);
        return (n + a).toUpperCase();
    }
}

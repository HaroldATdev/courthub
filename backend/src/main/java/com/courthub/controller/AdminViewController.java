package com.courthub.controller;

import com.courthub.dto.AdminDtos.*;
import com.courthub.dto.CanchaDto;
import com.courthub.dto.ClaseDto;
import com.courthub.dto.ReservaDtos.ReservaDto;
import com.courthub.dto.form.Formularios.CanchaForm;
import com.courthub.dto.form.Formularios.ClaseForm;
import com.courthub.dto.form.Formularios.UsuarioForm;
import com.courthub.exception.ApiException;
import com.courthub.security.AuthUser;
import com.courthub.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Panel ERP del administrador (vistas Thymeleaf bajo /admin/**).
 * Resumen con indicadores y gráficos, y mantenimiento de reservas, canchas, clases, inscripciones y usuarios.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminViewController {

    private final AdminService admin;

    /* =====================================================================
       RESUMEN
       ===================================================================== */

    @GetMapping({"", "/"})
    @SuppressWarnings("unchecked")
    public String resumen(Model model) {
        Map<String, Object> d = admin.dashboard();
        model.addAttribute("d", d);
        model.addAttribute("grafico", GraficoBarras.de((List<Map<String, Object>>) d.get("ingresosPorMes")));
        model.addAttribute("seccion", "resumen");
        return "admin/resumen";
    }

    /** Geometría de un gráfico de barras SVG (una serie) calculada en el servidor. */
    public record GraficoBarras(int ancho, int alto, double baseY, List<Barra> barras, List<Linea> grid) {
        public record Barra(String mes, BigDecimal total, Object reservas, double x, double y, double h, double w, boolean maximo) {}
        public record Linea(double y, long etiqueta) {}

        static GraficoBarras de(List<Map<String, Object>> datos) {
            int W = 640, H = 220, padL = 56, padR = 12, padT = 18, padB = 34;
            double alto = H - padT - padB;
            BigDecimal max = datos.stream().map(m -> (BigDecimal) m.get("total")).max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            long escala = Math.max(4, (long) Math.ceil(max.doubleValue() / 4) * 4);
            double ancho = (double) (W - padL - padR) / Math.max(1, datos.size());
            double barW = Math.min(44, ancho * 0.55);
            List<Barra> barras = new ArrayList<>();
            for (int i = 0; i < datos.size(); i++) {
                Map<String, Object> m = datos.get(i);
                BigDecimal total = (BigDecimal) m.get("total");
                double h = alto * total.doubleValue() / escala;
                double x = padL + ancho * i + (ancho - barW) / 2;
                barras.add(new Barra((String) m.get("mes"), total, m.get("reservas"), x, padT + alto - h, h, barW,
                        total.signum() > 0 && total.compareTo(max) == 0));
            }
            List<Linea> grid = new ArrayList<>();
            for (int g = 0; g <= 4; g++) grid.add(new Linea(padT + alto - (alto * g / 4), escala * g / 4));
            return new GraficoBarras(W, H, padT + alto, barras, grid);
        }
    }

    /* =====================================================================
       RESERVAS
       ===================================================================== */

    @GetMapping("/reservas")
    public String reservas(@RequestParam(defaultValue = "") String estado, @RequestParam(defaultValue = "") String desde,
                           @RequestParam(defaultValue = "") String hasta, @RequestParam(required = false) Long canchaId,
                           @RequestParam(defaultValue = "") String q, HttpServletRequest req, Model model) {
        List<ReservaDto> lista = admin.reservas(estado, desde, hasta, canchaId, q);
        BigDecimal total = lista.stream().filter(r -> !"cancelada".equals(r.estado()))
                .map(ReservaDto::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("reservas", lista);
        model.addAttribute("totalFiltro", total);
        model.addAttribute("canchas", admin.canchas());
        model.addAttribute("f", Map.of("estado", estado, "desde", desde, "hasta", hasta, "q", q,
                "canchaId", canchaId == null ? "" : canchaId.toString()));
        model.addAttribute("volver", req.getQueryString() == null ? "" : req.getQueryString());
        model.addAttribute("seccion", "reservas");
        return "admin/reservas";
    }

    @PostMapping("/reservas/{id}/estado")
    public String estadoReserva(@PathVariable Long id, @RequestParam String estado,
                                @RequestParam(defaultValue = "") String volver, RedirectAttributes ra) {
        try {
            admin.estadoReserva(id, new EstadoRequest(estado, null, null));
            ra.addFlashAttribute("toast", "Reserva actualizada a \"" + estado + "\".");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/admin/reservas" + (volver.isBlank() ? "" : "?" + volver);
    }

    @GetMapping(value = "/reservas/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportarReservas(@RequestParam(defaultValue = "") String estado, @RequestParam(defaultValue = "") String desde,
                                                   @RequestParam(defaultValue = "") String hasta, @RequestParam(required = false) Long canchaId,
                                                   @RequestParam(defaultValue = "") String q) {
        StringBuilder sb = new StringBuilder("﻿Codigo;Cliente;Email;Cancha;Fecha;Inicio;Fin;Subtotal;Cargo;Total;Metodo;Estado\n");
        for (ReservaDto r : admin.reservas(estado, desde, hasta, canchaId, q)) {
            sb.append(csv(r.codigo(), r.cliente() == null ? "" : r.cliente().nombre(), r.cliente() == null ? "" : r.cliente().email(),
                    r.cancha().nombre(), r.fecha(), r.horaInicio(), r.horaFin(), r.subtotal(), r.cargoServicio(), r.total(),
                    r.metodoPago(), r.estado()));
        }
        return descarga("reservas-courthub.csv", sb.toString());
    }

    /* =====================================================================
       CANCHAS
       ===================================================================== */

    @GetMapping("/canchas")
    public String canchas(Model model) {
        model.addAttribute("canchas", admin.canchas());
        model.addAttribute("seccion", "canchas");
        return "admin/canchas";
    }

    @GetMapping("/canchas/nueva")
    public String nuevaCancha(Model model) {
        model.addAttribute("canchaForm", new CanchaForm());
        return formCancha(model, null);
    }

    @PostMapping("/canchas")
    public String crearCancha(@Valid @ModelAttribute CanchaForm canchaForm, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formCancha(model, null);
        try {
            CanchaDto c = admin.crearCancha(canchaForm.aRequest());
            ra.addFlashAttribute("toast", "Cancha \"" + c.nombre() + "\" creada.");
            return "redirect:/admin/canchas";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formCancha(model, null);
        }
    }

    @GetMapping("/canchas/{id}/editar")
    public String editarCancha(@PathVariable Long id, Model model) {
        CanchaDto c = admin.canchas().stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Cancha no encontrada."));
        model.addAttribute("canchaForm", CanchaForm.de(c));
        return formCancha(model, id);
    }

    @PostMapping("/canchas/{id}")
    public String actualizarCancha(@PathVariable Long id, @Valid @ModelAttribute CanchaForm canchaForm, BindingResult result,
                                   Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formCancha(model, id);
        try {
            admin.actualizarCancha(id, canchaForm.aRequest());
            ra.addFlashAttribute("toast", "Cancha actualizada.");
            return "redirect:/admin/canchas";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formCancha(model, id);
        }
    }

    @PostMapping("/canchas/{id}/estado")
    public String estadoCancha(@PathVariable Long id, RedirectAttributes ra) {
        CanchaDto c = admin.estadoCancha(id, new EstadoRequest(null, null, null));
        ra.addFlashAttribute("toast", "Cancha " + (c.activa() ? "activada" : "desactivada") + ".");
        ra.addFlashAttribute("toastTipo", "info");
        return "redirect:/admin/canchas";
    }

    private String formCancha(Model model, Long id) {
        model.addAttribute("canchaId", id);
        model.addAttribute("seccion", "canchas");
        return "admin/cancha-form";
    }

    /* =====================================================================
       CLASES
       ===================================================================== */

    @GetMapping("/clases")
    public String clases(Model model) {
        model.addAttribute("clases", admin.clases());
        model.addAttribute("seccion", "clases");
        return "admin/clases";
    }

    @GetMapping("/clases/nueva")
    public String nuevaClase(Model model) {
        ClaseForm f = new ClaseForm();
        f.setFecha(com.courthub.util.Horarios.hoy().toString());
        model.addAttribute("claseForm", f);
        return formClase(model, null);
    }

    @PostMapping("/clases")
    public String crearClase(@Valid @ModelAttribute ClaseForm claseForm, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formClase(model, null);
        try {
            ClaseDto k = admin.crearClase(claseForm.aRequest());
            ra.addFlashAttribute("toast", "Clase \"" + k.nombre() + "\" programada.");
            return "redirect:/admin/clases";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formClase(model, null);
        }
    }

    @GetMapping("/clases/{id}/editar")
    public String editarClase(@PathVariable Long id, Model model) {
        ClaseDto k = admin.clases().stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        model.addAttribute("claseForm", ClaseForm.de(k));
        return formClase(model, id);
    }

    @PostMapping("/clases/{id}")
    public String actualizarClase(@PathVariable Long id, @Valid @ModelAttribute ClaseForm claseForm, BindingResult result,
                                  Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formClase(model, id);
        try {
            admin.actualizarClase(id, claseForm.aRequest());
            ra.addFlashAttribute("toast", "Clase actualizada.");
            return "redirect:/admin/clases";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formClase(model, id);
        }
    }

    @PostMapping("/clases/{id}/estado")
    public String estadoClase(@PathVariable Long id, RedirectAttributes ra) {
        ClaseDto k = admin.estadoClase(id, new EstadoRequest(null, null, null));
        ra.addFlashAttribute("toast", "Clase " + (k.activa() ? "activada" : "desactivada") + ".");
        ra.addFlashAttribute("toastTipo", "info");
        return "redirect:/admin/clases";
    }

    @GetMapping("/clases/{id}/inscripciones")
    public String inscripcionesDeClase(@PathVariable Long id, Model model) {
        ClaseDto k = admin.clases().stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Clase no encontrada."));
        model.addAttribute("clase", k);
        model.addAttribute("inscripciones", admin.inscripcionesDeClase(id));
        model.addAttribute("seccion", "clases");
        return "admin/clase-inscripciones";
    }

    private String formClase(Model model, Long id) {
        model.addAttribute("claseId", id);
        model.addAttribute("entrenadores", admin.entrenadores());
        model.addAttribute("canchasActivas", admin.canchas().stream().filter(c -> Boolean.TRUE.equals(c.activa())).toList());
        model.addAttribute("seccion", "clases");
        return "admin/clase-form";
    }

    /* =====================================================================
       INSCRIPCIONES
       ===================================================================== */

    @GetMapping("/inscripciones")
    public String inscripciones(@RequestParam(defaultValue = "") String estado, Model model) {
        model.addAttribute("inscripciones", admin.inscripciones(estado));
        model.addAttribute("estado", estado);
        model.addAttribute("seccion", "inscripciones");
        return "admin/inscripciones";
    }

    @PostMapping("/inscripciones/{id}/estado")
    public String estadoInscripcion(@PathVariable Long id, @RequestParam String estado,
                                    @RequestParam(defaultValue = "") String filtro, RedirectAttributes ra) {
        try {
            admin.estadoInscripcion(id, new EstadoRequest(estado, null, null));
            ra.addFlashAttribute("toast", "Inscripción actualizada.");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/admin/inscripciones" + (filtro.isBlank() ? "" : "?estado=" + filtro);
    }

    /* =====================================================================
       USUARIOS
       ===================================================================== */

    @GetMapping("/usuarios")
    public String usuarios(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String rol,
                           @RequestParam(defaultValue = "") String estado, HttpServletRequest req, Model model) {
        model.addAttribute("usuarios", admin.usuarios(q, rol, estado));
        model.addAttribute("q", q);
        model.addAttribute("rol", rol);
        model.addAttribute("estado", estado);
        model.addAttribute("volver", req.getQueryString() == null ? "" : req.getQueryString());
        model.addAttribute("seccion", "usuarios");
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuarioForm", new UsuarioForm());
        return formUsuario(model, null);
    }

    @PostMapping("/usuarios")
    public String crearUsuario(@Valid @ModelAttribute UsuarioForm usuarioForm, BindingResult result, Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formUsuario(model, null);
        try {
            UsuarioAdminDto u = admin.crearUsuario(usuarioForm.aRequest());
            ra.addFlashAttribute("toast", "Usuario " + u.email() + " creado.");
            return "redirect:/admin/usuarios";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formUsuario(model, null);
        }
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable Long id, Model model) {
        UsuarioAdminDto u = admin.usuarios("", "", "").stream().filter(x -> x.id().equals(id)).findFirst()
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado."));
        model.addAttribute("usuarioForm", UsuarioForm.de(u));
        return formUsuario(model, id);
    }

    @PostMapping("/usuarios/{id}")
    public String actualizarUsuario(@PathVariable Long id, @Valid @ModelAttribute UsuarioForm usuarioForm, BindingResult result,
                                    Model model, RedirectAttributes ra) {
        if (result.hasErrors()) return formUsuario(model, id);
        try {
            admin.actualizarUsuario(id, usuarioForm.aRequest());
            ra.addFlashAttribute("toast", "Usuario actualizado.");
            return "redirect:/admin/usuarios";
        } catch (ApiException e) {
            model.addAttribute("formError", e.getMessage());
            return formUsuario(model, id);
        }
    }

    /** Cambio de estado de la cuenta: activo (solo el administrador activa), pendiente o anulado. */
    @PostMapping("/usuarios/{id}/estado")
    public String estadoUsuario(@AuthenticationPrincipal AuthUser adminUser, @PathVariable Long id,
                                @RequestParam String estado, @RequestParam(defaultValue = "") String volver, RedirectAttributes ra) {
        try {
            UsuarioAdminDto u = admin.estadoUsuario(adminUser, id, estado);
            ra.addFlashAttribute("toast", "Cuenta de " + u.nombre() + " marcada como \"" + estado + "\".");
            ra.addFlashAttribute("toastTipo", "info");
        } catch (ApiException e) {
            ra.addFlashAttribute("toast", e.getMessage());
            ra.addFlashAttribute("toastTipo", "error");
        }
        return "redirect:/admin/usuarios" + (volver.isBlank() ? "" : "?" + volver);
    }

    @GetMapping(value = "/usuarios/exportar", produces = "text/csv")
    public ResponseEntity<byte[]> exportarUsuarios(@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "") String rol,
                                                   @RequestParam(defaultValue = "") String estado) {
        StringBuilder sb = new StringBuilder("﻿DNI;Usuario;Nombre;Apellido;Email;Perfil;Estado;Telefono;Nivel;Deporte;Registro\n");
        for (UsuarioAdminDto u : admin.usuarios(q, rol, estado)) {
            sb.append(csv(u.dni(), u.usuario(), u.nombre(), u.apellido(), u.email(), u.rol(), u.estado(), u.telefono(), u.nivel(),
                    u.deporte(), u.fechaRegistro()));
        }
        return descarga("usuarios-courthub.csv", sb.toString());
    }

    private String formUsuario(Model model, Long id) {
        model.addAttribute("usuarioId", id);
        model.addAttribute("seccion", "usuarios");
        return "admin/usuario-form";
    }

    /* ---------------- CSV ---------------- */

    private static String csv(Object... campos) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < campos.length; i++) {
            if (i > 0) sb.append(';');
            sb.append('"').append(String.valueOf(campos[i] == null ? "" : campos[i]).replace("\"", "\"\"")).append('"');
        }
        return sb.append('\n').toString();
    }

    private static ResponseEntity<byte[]> descarga(String nombre, String contenido) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(contenido.getBytes(StandardCharsets.UTF_8));
    }
}

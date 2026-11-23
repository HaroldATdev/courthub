package com.courthub.service;

import com.courthub.exception.ApiException;
import com.courthub.model.Clase;
import com.courthub.model.Pago;
import com.courthub.model.Reserva;
import com.courthub.model.Usuario;
import com.courthub.repository.PagoRepository;
import com.courthub.security.AuthUser;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Boleta de venta electrónica en PDF de un cobro de reserva. Serie B001 y número correlativo = id del pago.
 * El importe cobrado incluye IGV (18 %), que se desglosa en el comprobante.
 * Es un comprobante de demostración: no está enviado a SUNAT ni tiene validez tributaria.
 */
@Service
@RequiredArgsConstructor
public class BoletaService {

    public static final String SERIE = "B001";
    private static final BigDecimal IGV = new BigDecimal("1.18");
    private static final Color VERDE = new Color(5, 150, 105);
    private static final Color GRIS = new Color(100, 116, 139);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final PagoRepository pagos;
    private final com.courthub.repository.InscripcionRepository inscripciones;

    @Value("${courthub.emisor.razon-social:CourtHub S.A.C.}")
    private String razonSocial;

    @Value("${courthub.emisor.ruc:20000000001}")
    private String ruc;

    @Value("${courthub.emisor.direccion:Lima, Perú}")
    private String direccion;

    /** Resultado: nombre sugerido del archivo y su contenido. */
    public record Boleta(String nombreArchivo, byte[] pdf) {}

    private static final String SIN_BOLETA = "No hay una boleta: el pago no existe o no está aprobado.";

    /** Boleta de una reserva. Solo el dueño de la reserva o un administrador pueden pedirla. */
    @Transactional(readOnly = true)
    public Boleta generar(Long reservaId, AuthUser solicitante) {
        return de(pagos.findFirstByReservaIdAndEstadoInOrderByIdDesc(reservaId, List.of(Pago.APROBADO)), solicitante);
    }

    /** Boleta de la inscripción del propio usuario a una clase. */
    @Transactional(readOnly = true)
    public Boleta generarDeClase(Long claseId, AuthUser solicitante) {
        var inscripcion = inscripciones.findByClaseIdAndUsuarioId(claseId, solicitante.id());
        return de(inscripcion.flatMap(i -> pagos.findFirstByInscripcionIdAndEstadoInOrderByIdDesc(i.getId(), List.of(Pago.APROBADO))), solicitante);
    }

    /** Boleta por número de pago (la usa el reporte de ventas del administrador). */
    @Transactional(readOnly = true)
    public Boleta generarPorPago(Long pagoId, AuthUser solicitante) {
        return de(pagos.findWithOrigenById(pagoId).filter(p -> Pago.APROBADO.equals(p.getEstado())), solicitante);
    }

    private Boleta de(java.util.Optional<Pago> pago, AuthUser solicitante) {
        Pago p = pago.orElseThrow(() -> ApiException.notFound(SIN_BOLETA));
        boolean esDueno = p.getUsuario().getId().equals(solicitante.id());
        if (!esDueno && !solicitante.esAdmin()) throw ApiException.notFound(SIN_BOLETA);
        String numero = SERIE + "-" + String.format("%08d", p.getId());
        return new Boleta("boleta-" + numero + ".pdf", render(p, numero));
    }

    private byte[] render(Pago p, String numero) {
        Usuario u = p.getUsuario();
        BigDecimal total = p.getMonto();
        BigDecimal base = total.divide(IGV, 2, RoundingMode.HALF_UP);
        BigDecimal igv = total.subtract(base);

        Font titulo = new Font(Font.HELVETICA, 20, Font.BOLD, VERDE);
        Font negrita = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 10);
        Font tenue = new Font(Font.HELVETICA, 8, Font.NORMAL, GRIS);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 48, 48);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            // Cabecera: emisor a la izquierda, recuadro de la boleta a la derecha
            PdfPTable cab = new PdfPTable(new float[]{3f, 2f});
            cab.setWidthPercentage(100);
            PdfPCell emisor = sinBorde();
            emisor.addElement(new Paragraph("CourtHub", titulo));
            emisor.addElement(new Paragraph(razonSocial, negrita));
            emisor.addElement(new Paragraph(direccion, normal));
            emisor.addElement(new Paragraph("Reserva de canchas deportivas", tenue));
            cab.addCell(emisor);
            PdfPCell caja = new PdfPCell();
            caja.setBorderColor(VERDE);
            caja.setBorderWidth(1.5f);
            caja.setPadding(10);
            Paragraph l1 = new Paragraph("RUC " + ruc, negrita); l1.setAlignment(Element.ALIGN_CENTER);
            Paragraph l2 = new Paragraph("BOLETA DE VENTA ELECTRÓNICA", new Font(Font.HELVETICA, 11, Font.BOLD, VERDE));
            l2.setAlignment(Element.ALIGN_CENTER); l2.setSpacingBefore(4); l2.setSpacingAfter(4);
            Paragraph l3 = new Paragraph(numero, new Font(Font.HELVETICA, 12, Font.BOLD)); l3.setAlignment(Element.ALIGN_CENTER);
            caja.addElement(l1); caja.addElement(l2); caja.addElement(l3);
            cab.addCell(caja);
            doc.add(cab);

            // Datos del cliente y de la operación
            PdfPTable datos = new PdfPTable(new float[]{1.2f, 3.8f});
            datos.setWidthPercentage(100);
            datos.setSpacingBefore(22);
            fila(datos, "Cliente", u.getNombre() + " " + u.getApellido(), negrita, normal);
            fila(datos, "DNI", u.getDni() == null || u.getDni().isBlank() ? "—" : u.getDni(), negrita, normal);
            fila(datos, "Correo", u.getEmail(), negrita, normal);
            fila(datos, "Fecha de emisión", p.getFechaPago().atZoneSameInstant(com.courthub.util.Horarios.LIMA).format(FECHA_HORA), negrita, normal);
            fila(datos, "N.º de operación", p.getOperacion(), negrita, normal);
            fila(datos, "Medio de pago", p.getMetodo() + (p.getTarjetaUltimos4() == null ? "" : " terminada en " + p.getTarjetaUltimos4()), negrita, normal);
            doc.add(datos);

            // Detalle
            PdfPTable det = new PdfPTable(new float[]{4.5f, 1f, 1.5f});
            det.setWidthPercentage(100);
            det.setSpacingBefore(20);
            encabezado(det, "Descripción");
            encabezado(det, "Cant.");
            PdfPCell hImp = encabezadoCelda("Importe (S/)");
            hImp.setHorizontalAlignment(Element.ALIGN_RIGHT);
            det.addCell(hImp);
            PdfPCell desc = new PdfPCell();
            desc.setPadding(7);
            desc.setBorder(Rectangle.BOTTOM);
            desc.setBorderColor(Color.LIGHT_GRAY);
            if (p.esDeReserva()) {
                Reserva r = p.getReserva();
                desc.addElement(new Paragraph("Reserva " + r.getCodigo() + " · " + r.getCancha().getNombre(), normal));
                desc.addElement(new Paragraph(r.getFecha().format(FECHA) + " de " + r.getHoraInicio() + " a " + r.getHoraFin() + " (bloque de 1.5 h)", tenue));
            } else {
                Clase k = p.getInscripcion().getClase();
                desc.addElement(new Paragraph("Inscripción a clase: " + k.getNombre(), normal));
                desc.addElement(new Paragraph(k.getFecha().format(FECHA) + " de " + k.getHoraInicio() + " a " + k.getHoraFin(), tenue));
            }
            det.addCell(desc);
            det.addCell(celda("1", Element.ALIGN_CENTER, normal, true));
            det.addCell(celda(dinero(p.getSubtotal()), Element.ALIGN_RIGHT, normal, true));
            if (p.getCargoServicio().signum() > 0) {
                det.addCell(celda("Cargo por servicio (10 %)", Element.ALIGN_LEFT, normal, true));
                det.addCell(celda("1", Element.ALIGN_CENTER, normal, true));
                det.addCell(celda(dinero(p.getCargoServicio()), Element.ALIGN_RIGHT, normal, true));
            }
            doc.add(det);

            // Totales con IGV desglosado
            PdfPTable tot = new PdfPTable(new float[]{4.5f, 2.5f});
            tot.setWidthPercentage(100);
            tot.setSpacingBefore(10);
            totalFila(tot, "Op. gravada", dinero(base), normal);
            totalFila(tot, "IGV (18 %)", dinero(igv), normal);
            totalFila(tot, "IMPORTE TOTAL", "S/ " + dinero(total), negrita);
            doc.add(tot);

            Paragraph pie = new Paragraph("Estado del pago: " + p.getEstado().toUpperCase(Locale.ROOT)
                    + " · Proveedor: " + p.getProveedor()
                    + "\nComprobante de demostración del proyecto universitario CourtHub: no ha sido enviado a SUNAT y no tiene validez tributaria."
                    + "\nGracias por reservar con CourtHub.", tenue);
            pie.setSpacingBefore(28);
            doc.add(pie);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar la boleta", e);
        } finally {
            doc.close();
        }
        return out.toByteArray();
    }

    private static String dinero(BigDecimal v) {
        return String.format(Locale.ROOT, "%,.2f", v);
    }

    private static PdfPCell sinBorde() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static void fila(PdfPTable t, String k, String v, Font fk, Font fv) {
        PdfPCell a = new PdfPCell(new Phrase(k, fk)); a.setBorder(Rectangle.NO_BORDER); a.setPadding(3);
        PdfPCell b = new PdfPCell(new Phrase(v, fv)); b.setBorder(Rectangle.NO_BORDER); b.setPadding(3);
        t.addCell(a); t.addCell(b);
    }

    private static void encabezado(PdfPTable t, String texto) {
        t.addCell(encabezadoCelda(texto));
    }

    private static PdfPCell encabezadoCelda(String texto) {
        PdfPCell c = new PdfPCell(new Phrase(texto, new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE)));
        c.setBackgroundColor(VERDE);
        c.setPadding(6);
        c.setBorder(Rectangle.NO_BORDER);
        return c;
    }

    private static PdfPCell celda(String texto, int alineacion, Font f, boolean linea) {
        PdfPCell c = new PdfPCell(new Phrase(texto, f));
        c.setHorizontalAlignment(alineacion);
        c.setPadding(7);
        c.setBorder(linea ? Rectangle.BOTTOM : Rectangle.NO_BORDER);
        c.setBorderColor(Color.LIGHT_GRAY);
        return c;
    }

    private static void totalFila(PdfPTable t, String k, String v, Font f) {
        PdfPCell a = celda(k, Element.ALIGN_RIGHT, f, false);
        PdfPCell b = celda(v, Element.ALIGN_RIGHT, f, false);
        t.addCell(a); t.addCell(b);
    }
}

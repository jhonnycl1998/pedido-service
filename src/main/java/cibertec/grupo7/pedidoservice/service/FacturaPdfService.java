package cibertec.grupo7.pedidoservice.service;

import cibertec.grupo7.pedidoservice.model.entity.DetallePedido;
import cibertec.grupo7.pedidoservice.model.entity.Pedido;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class FacturaPdfService {

    public byte[] generarFacturaEnMemoria(Pedido pedido) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
            Font fontCabecera = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            Paragraph titulo = new Paragraph("FACTURA DE COMPRA - TIENDA DE ROPA DAW2", fontTitulo);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(Chunk.NEWLINE);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            document.add(new Paragraph("N° Pedido: " + pedido.getIdpedido(), fontNormal));
            document.add(new Paragraph("ID Cliente: " + pedido.getIdusuario(), fontNormal));
            document.add(new Paragraph("Fecha: " + pedido.getFechaPedido().format(fmt), fontNormal));
            document.add(new Paragraph("Estado: " + pedido.getEstado(), fontNormal));
            document.add(Chunk.NEWLINE);

            PdfPTable tabla = new PdfPTable(5);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{4f, 1.2f, 1.2f, 2f, 2f});

            String[] columnas = {"Producto", "Talla", "Cant.", "P. Unitario", "Subtotal"};
            for (String col : columnas) {
                PdfPCell cell = new PdfPCell(new Phrase(col, fontCabecera));
                cell.setBackgroundColor(new Color(40, 40, 40));
                cell.setPadding(6);
                tabla.addCell(cell);
            }

            for (DetallePedido det : pedido.getDetalles()) {
                tabla.addCell(new Phrase(det.getNombreProducto(), fontNormal));
                tabla.addCell(new Phrase(det.getTalla(), fontNormal));
                tabla.addCell(new Phrase(String.valueOf(det.getCantidad()), fontNormal));
                tabla.addCell(new Phrase("S/ " + det.getPrecioUnitario(), fontNormal));
                tabla.addCell(new Phrase("S/ " + det.getSubtotal(), fontNormal));
            }

            document.add(tabla);
            document.add(Chunk.NEWLINE);

            Paragraph total = new Paragraph("TOTAL: S/ " + pedido.getTotal(), fontTitulo);
            total.setAlignment(Element.ALIGN_RIGHT);
            document.add(total);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar factura PDF", e);
        }
    }
}
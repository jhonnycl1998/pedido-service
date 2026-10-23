package cibertec.grupo7.pedidoservice.controller;

import cibertec.grupo7.pedidoservice.exception.StockInsuficienteException;
import cibertec.grupo7.pedidoservice.model.entity.Pedido;
import cibertec.grupo7.pedidoservice.service.CarritoPedidoService;
import cibertec.grupo7.pedidoservice.service.FacturaPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/pedidos", "/api/pedidos"}) // Soporta ambas rutas (/api/v1/pedidos y /api/pedidos/{id}/factura)
@RequiredArgsConstructor
public class PedidoController {

    private final CarritoPedidoService carritoPedidoService;
    private final FacturaPdfService facturaPdfService;

    private Integer obtenerIdUsuario(Authentication auth) {
        return Integer.parseInt(auth.getCredentials().toString());
    }

    // CLIENTE: Comprar (Checkout @Transactional -> HTTP 409 si no hay stock, HTTP 400 si está vacío)
    @PostMapping("/checkout")
    public ResponseEntity<?> realizarCompra(Authentication auth) {
        try {
            Pedido pedido = carritoPedidoService.comprar(obtenerIdUsuario(auth));
            return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
        } catch (StockInsuficienteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    // CLIENTE: Ver su propio historial de compras
    @GetMapping("/mis-pedidos")
    public ResponseEntity<List<Pedido>> verMiHistorial(Authentication auth) {
        return ResponseEntity.ok(carritoPedidoService.historialCliente(obtenerIdUsuario(auth)));
    }

    // ADMIN: Ver todos los pedidos
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Pedido>> verTodosLosPedidos() {
        return ResponseEntity.ok(carritoPedidoService.listarTodosPedidosAdmin());
    }

    // Descargar factura PDF en memoria: GET /api/pedidos/{id}/factura
    @GetMapping("/{id}/factura")
    public ResponseEntity<byte[]> descargarFactura(@PathVariable("id") Integer idpedido, Authentication auth) {
        Integer idUsuario = obtenerIdUsuario(auth);
        Pedido pedido = carritoPedidoService.obtenerPedidoPorId(idpedido);

        if(!pedido.getIdusuario().equals(idUsuario)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }


        byte[] pdfBytes = facturaPdfService.generarFacturaEnMemoria(pedido);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=factura_" + idpedido + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
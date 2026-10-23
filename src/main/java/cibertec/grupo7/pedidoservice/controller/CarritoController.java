package cibertec.grupo7.pedidoservice.controller;

import cibertec.grupo7.pedidoservice.exception.StockInsuficienteException;
import cibertec.grupo7.pedidoservice.model.entity.CarritoItem;
import cibertec.grupo7.pedidoservice.service.CarritoPedidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoPedidoService carritoPedidoService;

    private Integer obtenerIdUsuario(Authentication auth) {
        return Integer.parseInt(auth.getCredentials().toString());
    }

    @GetMapping
    public ResponseEntity<List<CarritoItem>> obtenerMiCarrito(Authentication auth) {
        return ResponseEntity.ok(carritoPedidoService.verCarrito(obtenerIdUsuario(auth)));
    }

    @PostMapping("/agregar")
    public ResponseEntity<?> agregarItem(
            Authentication auth,
            @RequestBody Map<String, Integer> body) {
        try {
            CarritoItem item = carritoPedidoService.agregarAlCarrito(
                    obtenerIdUsuario(auth),
                    body.get("idproducto"),
                    body.get("cantidad")
            );
            return ResponseEntity.ok(item);
        } catch (StockInsuficienteException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{idproducto}")
    public ResponseEntity<Void> eliminarItem(Authentication auth, @PathVariable Integer idproducto) {
        carritoPedidoService.eliminarItemCarrito(obtenerIdUsuario(auth), idproducto);
        return ResponseEntity.noContent().build();
    }
}
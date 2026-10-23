package cibertec.grupo7.pedidoservice.service;

import cibertec.grupo7.pedidoservice.exception.StockInsuficienteException;
import cibertec.grupo7.pedidoservice.feign.CatalogoFeignClient;
import cibertec.grupo7.pedidoservice.model.dto.ProductoDto;
import cibertec.grupo7.pedidoservice.model.entity.CarritoItem;
import cibertec.grupo7.pedidoservice.model.entity.DetallePedido;
import cibertec.grupo7.pedidoservice.model.entity.Pedido;
import cibertec.grupo7.pedidoservice.repository.CarritoItemRepository;
import cibertec.grupo7.pedidoservice.repository.PedidoRepository;
import feign.FeignException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CarritoPedidoService {

    private final CarritoItemRepository carritoRepository;
    private final PedidoRepository pedidoRepository;
    private final CatalogoFeignClient catalogoFeignClient;

    public List<CarritoItem> verCarrito(Integer idusuario) {
        return carritoRepository.findByIdusuario(idusuario);
    }

    // 1. El carrito valida stock al agregar (vía OpenFeign), pero NO lo descuenta
    @Transactional
    public CarritoItem agregarAlCarrito(Integer idusuario, Integer idproducto, Integer cantidad) {
        ProductoDto producto;
        try {
            producto = catalogoFeignClient.obtenerProducto(idproducto);
        } catch (FeignException ex) {
            throw new RuntimeException("No se pudo obtener el producto " + idproducto + " del catálogo");
        }

        CarritoItem item = carritoRepository.findByIdusuarioAndIdproducto(idusuario, idproducto)
                .orElse(CarritoItem.builder()
                        .idusuario(idusuario)
                        .idproducto(idproducto)
                        .cantidad(0)
                        .build());

        int nuevaCantidad = item.getCantidad() + cantidad;
        if (producto.getStock() < nuevaCantidad) {
            throw new StockInsuficienteException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        item.setCantidad(nuevaCantidad);
        return carritoRepository.save(item);
    }

    @Transactional
    public void eliminarItemCarrito(Integer idusuario, Integer idproducto) {
        carritoRepository.findByIdusuarioAndIdproducto(idusuario, idproducto)
                .ifPresent(carritoRepository::delete);
    }

    // 2. Checkout @Transactional: usa UPDATE condicional. Si devuelve 0 filas -> StockInsuficienteException -> Rollback total -> HTTP 409
    @Transactional(rollbackOn = Exception.class)
    public Pedido comprar(Integer idusuario) {
        List<CarritoItem> items = carritoRepository.findByIdusuario(idusuario);
        if (items.isEmpty()) {
            throw new RuntimeException("El carrito está vacío.");
        }

        Pedido pedido = Pedido.builder()
                .idusuario(idusuario)
                .fechaPedido(LocalDateTime.now())
                .estado("COMPLETADO")
                .build();

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CarritoItem item : items) {
            ProductoDto prod;
            try{
                prod = catalogoFeignClient.obtenerProducto(item.getIdproducto());
            }catch (FeignException ex){
                throw new RuntimeException("No se pudo obtener el producto " + item.getIdproducto() + " del catálogo");
            }

            // Ejecuta UPDATE tb_producto SET stock = stock - cantidad WHERE id_producto = ? AND stock >= cantidad
            int filas = carritoRepository.descontarStockAtomico(item.getIdproducto(), item.getCantidad());
            if (filas == 0) {
                throw new StockInsuficienteException("Producto agotado o sin stock suficiente: " + prod.getNombre());
            }

            BigDecimal subtotal = prod.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            total = total.add(subtotal);

            detalles.add(DetallePedido.builder()
                    .pedido(pedido)
                    .idproducto(prod.getIdproducto())
                    .nombreProducto(prod.getNombre())
                    .talla(prod.getTalla())
                    .precioUnitario(prod.getPrecio())
                    .cantidad(item.getCantidad())
                    .subtotal(subtotal)
                    .build());
        }

        pedido.setTotal(total);
        pedido.setDetalles(detalles);
        Pedido guardado = pedidoRepository.save(pedido);

        carritoRepository.deleteByIdusuario(idusuario);
        return guardado;
    }

    public List<Pedido> historialCliente(Integer idusuario) {
        return pedidoRepository.findByIdusuarioOrderByFechaPedidoDesc(idusuario);
    }

    public List<Pedido> listarTodosPedidosAdmin() {
        return pedidoRepository.findAllByOrderByFechaPedidoDesc();
    }

    public Pedido obtenerPedidoPorId(Integer idpedido) {
        return pedidoRepository.findById(idpedido)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
    }
}
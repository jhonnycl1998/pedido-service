package cibertec.grupo7.pedidoservice.feign;

import cibertec.grupo7.pedidoservice.model.dto.ProductoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "catalogo-service")
public interface CatalogoFeignClient {

    @GetMapping("/api/v1/productos/{idproducto}")
    ProductoDto obtenerProducto(@PathVariable("idproducto") Integer idproducto);

}
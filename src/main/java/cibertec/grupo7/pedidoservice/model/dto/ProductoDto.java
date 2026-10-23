package cibertec.grupo7.pedidoservice.model.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductoDto {
    private Integer idproducto;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private String genero;
    private String talla;
    private String categoria;
    private String marca;
    private String imagenUrl;
    private boolean activo;
}
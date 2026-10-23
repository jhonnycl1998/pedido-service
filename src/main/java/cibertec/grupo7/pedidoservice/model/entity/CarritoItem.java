package cibertec.grupo7.pedidoservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_carrito_item")
public class CarritoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrito_item")
    private Integer idcarritoitem;

    @Column(name = "id_usuario", nullable = false)
    private Integer idusuario;

    @Column(name = "id_producto", nullable = false)
    private Integer idproducto;

    @Column(name = "cantidad", nullable = false)
    private Integer cantidad;
}
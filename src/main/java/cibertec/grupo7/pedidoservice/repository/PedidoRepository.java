package cibertec.grupo7.pedidoservice.repository;

import cibertec.grupo7.pedidoservice.model.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
    List<Pedido> findByIdusuarioOrderByFechaPedidoDesc(Integer idusuario);
    List<Pedido> findAllByOrderByFechaPedidoDesc();
}
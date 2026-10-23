package cibertec.grupo7.pedidoservice.repository;

import cibertec.grupo7.pedidoservice.model.entity.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Integer> {
    List<CarritoItem> findByIdusuario(Integer idusuario);
    Optional<CarritoItem> findByIdusuarioAndIdproducto(Integer idusuario, Integer idproducto);
    void deleteByIdusuario(Integer idusuario);

    // UPDATE condicional atómico sobre tb_producto dentro de la misma transacción
    @Modifying
    @Query(value = "UPDATE tb_producto SET stock = stock - :cantidad WHERE id_producto = :idproducto AND stock >= :cantidad", nativeQuery = true)
    int descontarStockAtomico(@Param("idproducto") Integer idproducto, @Param("cantidad") Integer cantidad);
}
package com.restaurante.ms_despacho.repository;

import com.restaurante.ms_despacho.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, Long> {
    // Al ser una relación de 1 a 1 en la lógica de negocio (un pedido suele tener un único despacho), 
    // usamos Optional en caso de que aún no se haya creado la logística de ese pedido.
    Optional<Despacho> findByPedidoId(Long pedidoId);
}

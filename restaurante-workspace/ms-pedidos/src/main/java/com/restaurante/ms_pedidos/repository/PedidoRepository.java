package com.restaurante.ms_pedidos.repository;

import com.restaurante.ms_pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Muy útil para buscar el historial de compras de un cliente en específico
    List<Pedido> findByClienteId(Long clienteId);
}


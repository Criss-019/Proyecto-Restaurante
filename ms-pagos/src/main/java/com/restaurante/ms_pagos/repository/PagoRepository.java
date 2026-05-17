package com.restaurante.ms_pagos.repository;

import com.restaurante.ms_pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    // Esencial para revisar si un pedido específico ya fue pagado o para ver su historial de cobros
    List<Pago> findByPedidoId(Long pedidoId);
}

package com.restaurante.ms_facturacion.repository;

import com.restaurante.ms_facturacion.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    // Al ser una relación única por compra, buscamos la factura vinculada directamente a un pedido
    Optional<Factura> findByPedidoId(Long pedidoId);
    
    // Útil para validar auditorías o búsquedas por el folio impreso
    Optional<Factura> findByNumeroFactura(String numeroFactura);
}

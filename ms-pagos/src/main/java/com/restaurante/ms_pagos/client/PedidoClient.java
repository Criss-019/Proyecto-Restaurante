package com.restaurante.ms_pagos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

// Nos conectamos al orquestador usando su nombre registrado en Eureka
@FeignClient(name = "ms-pedidos", path = "/api/pedidos")
public interface PedidoClient {

    // Apuntamos al endpoint que actualiza el estado del pedido
    // Cambio de @PatchMapping a @PutMapping
    @PutMapping("/{id}/estado")
    void cambiarEstadoPedido(@PathVariable("id") Long id, @RequestParam("estado") String estado);
}

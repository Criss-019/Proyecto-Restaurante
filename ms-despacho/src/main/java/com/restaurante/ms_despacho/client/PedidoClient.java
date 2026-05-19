package com.restaurante.ms_despacho.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ms-pedidos", path = "/api/pedidos")
public interface PedidoClient {

    @PutMapping("/{id}/estado")
    void cambiarEstadoPedido(@PathVariable("id") Long id, @RequestParam("estado") String estado);
}

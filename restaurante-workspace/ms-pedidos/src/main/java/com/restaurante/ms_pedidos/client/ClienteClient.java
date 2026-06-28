package com.restaurante.ms_pedidos.client;

import com.restaurante.ms_pedidos.client.dto.ClienteDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// El "name" debe ser exactamente el spring.application.name de ms-clientes
@FeignClient(name = "ms-clientes", path = "/api/clientes")
public interface ClienteClient {

    // Replicamos la firma del endpoint que ya existe en ms-clientes
    @GetMapping("/{id}")
    ClienteDTO obtenerPorId(@PathVariable("id") Long id);
}

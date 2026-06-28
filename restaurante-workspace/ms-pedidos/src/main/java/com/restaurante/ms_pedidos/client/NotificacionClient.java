package com.restaurante.ms_pedidos.client;

import com.restaurante.ms_pedidos.client.dto.NotificacionRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

// Nos conectamos a ms-notificaciones a través de Eureka
@FeignClient(name = "ms-notificaciones", path = "/api/notificaciones")
public interface NotificacionClient {

    @PostMapping
    void enviarNotificacion(@RequestBody NotificacionRequestDTO request);
}

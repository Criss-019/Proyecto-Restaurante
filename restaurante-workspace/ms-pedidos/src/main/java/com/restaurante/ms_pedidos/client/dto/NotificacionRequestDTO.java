package com.restaurante.ms_pedidos.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificacionRequestDTO {
    private String destinatario; // El correo del cliente
    private String asunto;
    private String mensaje;
    private String tipo; // Ej: "EMAIL" o "SMS"
}

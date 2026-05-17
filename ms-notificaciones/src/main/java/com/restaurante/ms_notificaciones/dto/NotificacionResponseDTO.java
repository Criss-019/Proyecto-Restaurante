package com.restaurante.ms_notificaciones.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Mantenemos el estándar visual riguroso
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "clienteId", "destinatario", "tipo", "asunto", "mensaje", "estado", "fechaCreacion", "fechaEnvio"})
public class NotificacionResponseDTO {
    private Long id;
    private Long clienteId;
    private String destinatario;
    private String tipo;
    private String asunto;
    private String mensaje;
    private String estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaEnvio;
}


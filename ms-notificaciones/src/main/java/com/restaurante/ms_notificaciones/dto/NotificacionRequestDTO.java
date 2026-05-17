package com.restaurante.ms_notificaciones.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificacionRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El destinatario (email/teléfono) es obligatorio")
    private String destinatario;

    @NotBlank(message = "El tipo de notificación es obligatorio (Ej: EMAIL, SMS)")
    private String tipo;

    private String asunto; // Es opcional porque un SMS no lleva asunto

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String mensaje;
}


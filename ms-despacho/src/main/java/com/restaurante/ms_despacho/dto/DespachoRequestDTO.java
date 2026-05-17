package com.restaurante.ms_despacho.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class DespachoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotBlank(message = "El nombre del repartidor asignado es obligatorio")
    @Size(max = 100, message = "El nombre del repartidor no debe superar los 100 caracteres")
    private String repartidorAsignado;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    @Size(max = 255, message = "La dirección no debe superar los 255 caracteres")
    private String direccionEntrega;

    @NotNull(message = "La fecha y hora estimada de entrega es obligatoria")
    private LocalDateTime fechaEntregaEstimada;
}

package com.restaurante.ms_despacho.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Aplicamos tu anotación para forzar un orden visual limpio en Postman
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "pedidoId", "repartidorAsignado", "direccionEntrega", "estado", "fechaSalida", "fechaEntregaEstimada", "fechaEntrega"})
public class DespachoResponseDTO {
    private Long id;
    private Long pedidoId;
    private String repartidorAsignado;
    private String direccionEntrega;
    private String estado;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaEntregaEstimada;
    private LocalDateTime fechaEntrega;
}

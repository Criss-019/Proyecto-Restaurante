package com.restaurante.ms_reservas.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "clienteId", "fechaHora", "cantidadPersonas", "estado", "observaciones"})
public class ReservaResponseDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fechaHora;
    private Integer cantidadPersonas;
    private String estado;
    private String observaciones;
}

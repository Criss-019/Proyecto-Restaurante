package com.restaurante.ms_cocina.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// Mantenemos el orden visual estándar que solicitaste
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "pedidoId", "platoId", "cantidad", "estado", "notas"})
public class ComandaResponseDTO {
    private Long id;
    private Long pedidoId;
    private Long platoId;
    private Integer cantidad;
    private String estado;
    private String notas;
}


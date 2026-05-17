package com.restaurante.ms_stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class IngredienteResponseDTO {
    private Long id;
    private String nombre;
    private Double cantidadActual;
    private Double cantidadMinima;
    private String unidadMedida;
}

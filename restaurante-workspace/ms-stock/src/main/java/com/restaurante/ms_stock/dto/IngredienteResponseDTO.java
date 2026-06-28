package com.restaurante.ms_stock.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "nombre", "cantidadActual", "cantidadMinima", "unidadMedida"}) // Para forzar el orden visual en Postman
public class IngredienteResponseDTO {
    private Long id;
    private String nombre;
    private Double cantidadActual;
    private Double cantidadMinima;
    private String unidadMedida;
}

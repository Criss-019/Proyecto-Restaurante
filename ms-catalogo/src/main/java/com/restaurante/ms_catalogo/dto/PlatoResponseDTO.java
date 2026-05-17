package com.restaurante.ms_catalogo.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "nombre", "descripcion", "precio", "disponible"}) // Para forzar el orden visual en Postman
public class PlatoResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean disponible;
}

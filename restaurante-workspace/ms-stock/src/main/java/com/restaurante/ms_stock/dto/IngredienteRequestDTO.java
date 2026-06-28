package com.restaurante.ms_stock.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IngredienteRequestDTO {

    @NotBlank(message = "El nombre del ingrediente no puede estar vacio")
    @Size(min = 2, max = 50)
    private String nombre;

    @NotNull(message = "La cantidad actual es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Double cantidadActual;

    @NotNull(message = "La cantidad minima es obligatoria")
    @Min(value = 0, message = "La cantidad minima no puede ser negativa")
    private Double cantidadMinima;

    @NotBlank(message = "La unidad de medida es obligatoria (ej: Gramos, Unidades)")
    private String unidadMedida;
}



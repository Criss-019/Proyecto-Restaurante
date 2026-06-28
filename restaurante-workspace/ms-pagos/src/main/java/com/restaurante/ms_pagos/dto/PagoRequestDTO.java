package com.restaurante.ms_pagos.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoRequestDTO {

    @NotNull(message = "El ID del pedido es obligatorio")
    private Long pedidoId;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private Double monto;

    @NotBlank(message = "El metodo de pago es obligatorio")
    private String metodoPago;
    
    // No pedimos la fecha ni el estado, el sistema los asume como "ahora" y "APROBADO" por defecto en la creación básica.
}


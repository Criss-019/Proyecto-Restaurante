package com.restaurante.ms_pagos.client.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FacturaRequestDTO {
    private Long pedidoId;
    private Double subtotal;
}

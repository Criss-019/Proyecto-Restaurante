package com.restaurante.ms_pagos.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Manteniendo el estándar visual para Postman y futuras integraciones frontend
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "pedidoId", "monto", "metodoPago", "fechaPago", "estado"})
public class PagoResponseDTO {
    private Long id;
    private Long pedidoId;
    private Double monto;
    private String metodoPago;
    private LocalDateTime fechaPago;
    private String estado;
}


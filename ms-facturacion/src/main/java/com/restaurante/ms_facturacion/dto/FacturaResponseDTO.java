package com.restaurante.ms_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Aplicamos tu patrón de diseño para garantizar la visualización estructurada en Postman
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "pedidoId", "numeroFactura", "subtotal", "impuestos", "total", "fechaEmision", "estadoFiscal", "urlPdf"})
public class FacturaResponseDTO {
    private Long id;
    private Long pedidoId;
    private String numeroFactura;
    private Double subtotal;
    private Double impuestos;
    private Double total;
    private LocalDateTime fechaEmision;
    private String estadoFiscal;
    private String urlPdf;
}

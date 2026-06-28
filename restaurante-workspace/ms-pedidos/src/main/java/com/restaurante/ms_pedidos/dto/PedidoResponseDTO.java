package com.restaurante.ms_pedidos.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

// Ordenamos visualmente para que en Postman y el Frontend sea fácil de leer
@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "clienteId", "fechaPedido", "tipoEntrega", "estado", "total"})
public class PedidoResponseDTO {
    private Long id;
    private Long clienteId;
    private LocalDateTime fechaPedido;
    private String tipoEntrega;
    private String estado;
    private Double total;
}


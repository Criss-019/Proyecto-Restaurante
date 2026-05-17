package com.restaurante.ms_pedidos.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PedidoRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El tipo de entrega es obligatorio (Ej: MESA, DELIVERY)")
    private String tipoEntrega;

    // Nota: No pedimos el 'total' ni 'estado' en el Request porque son calculados/asignados por el sistema, no por el usuario.
}


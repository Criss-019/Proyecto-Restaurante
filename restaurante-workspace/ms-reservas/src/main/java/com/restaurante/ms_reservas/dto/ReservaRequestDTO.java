package com.restaurante.ms_reservas.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReservaRequestDTO {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "La fecha y hora de la reserva es obligatoria")
    @FutureOrPresent(message = "La reserva debe ser para una fecha actual o futura")
    private LocalDateTime fechaHora;

    @NotNull(message = "La cantidad de personas es obligatoria")
    @Min(value = 1, message = "La reserva debe ser para al menos 1 persona")
    @Max(value = 20, message = "Para reservas de más de 20 personas, contacte al local")
    private Integer cantidadPersonas;

    private String observaciones;
}
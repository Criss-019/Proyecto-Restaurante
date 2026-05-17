package com.restaurante.ms_clientes.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteRequestDTO {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un formato de email valido")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "El telefono debe contener entre 8 y 15 digitos numericos")
    private String telefono;

    @NotBlank(message = "La direccion de entrega es obligatoria")
    private String direccion;
}

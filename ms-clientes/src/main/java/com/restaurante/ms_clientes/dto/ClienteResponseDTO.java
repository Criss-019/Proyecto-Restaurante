package com.restaurante.ms_clientes.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@JsonPropertyOrder({"id", "nombre", "email", "telefono", "direccion"}) // Para forzar el orden visual en Postman
public class ClienteResponseDTO {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
}

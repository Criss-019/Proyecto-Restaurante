package com.restaurante.ms_pedidos.client.dto;

import lombok.Getter;
import lombok.Setter;

// Solo mapeamos los datos que nos interesan recibir del otro microservicio
@Getter
@Setter
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String email;
}

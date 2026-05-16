package com.restaurante.ms_catalogo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "platos")
@Data // Magia de Lombok: crea los Getters, Setters y toString automáticamente
@NoArgsConstructor // Lombok: crea un constructor vacío
@AllArgsConstructor // Lombok: crea un constructor con todos los atributos

public class Plato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private Double precio;
    private Boolean disponible;

}

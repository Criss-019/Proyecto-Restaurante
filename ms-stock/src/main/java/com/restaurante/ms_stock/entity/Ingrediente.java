package com.restaurante.ms_stock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ingredientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingrediente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private Double cantidadActual;

    @Column(nullable = false)
    private Double cantidadMinima; // Para alertas de stock bajo

    @Column(nullable = false)
    private String unidadMedida; // Ej: "Gramos", "Unidades", "Litros"
}

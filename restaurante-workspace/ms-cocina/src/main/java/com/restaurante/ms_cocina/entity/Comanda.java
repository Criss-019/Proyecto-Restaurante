package com.restaurante.ms_cocina.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comandas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica con ms-pedidos (para saber a qué orden global pertenece)
    @Column(nullable = false)
    private Long pedidoId;

    // Relación lógica con ms-catalogo (para saber qué plato es)
    @Column(nullable = false)
    private Long platoId;

    @Column(nullable = false)
    private Integer cantidad;

    // Estados típicos: PENDIENTE, EN_PREPARACION, LISTO
    @Column(nullable = false, length = 50)
    private String estado; 

    @Column(length = 255)
    private String notas; // Ej: "Sin cebolla", "Término medio"
}


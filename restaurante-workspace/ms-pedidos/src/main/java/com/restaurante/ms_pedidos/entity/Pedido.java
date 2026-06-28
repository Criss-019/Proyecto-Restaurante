package com.restaurante.ms_pedidos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica con ms-clientes
    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private LocalDateTime fechaPedido;

    // Ej: CREADO, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
    @Column(nullable = false, length = 50)
    private String estado; 

    // Ej: MESA, DELIVERY, PARA_LLEVAR
    @Column(nullable = false, length = 50)
    private String tipoEntrega;

    // El total se calculará más adelante sumando los platos, pero debe registrarse aquí
    @Column(nullable = false)
    private Double total; 
}


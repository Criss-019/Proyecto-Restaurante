package com.restaurante.ms_pagos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica con el microservicio ms-pedidos
    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false)
    private Double monto;

    // Ej: EFECTIVO, TARJETA_CREDITO, DEBITO, TRANSFERENCIA
    @Column(nullable = false, length = 50)
    private String metodoPago;

    @Column(nullable = false)
    private LocalDateTime fechaPago;

    // Ej: APROBADO, RECHAZADO, REEMBOLSADO
    @Column(nullable = false, length = 50)
    private String estado; 
}


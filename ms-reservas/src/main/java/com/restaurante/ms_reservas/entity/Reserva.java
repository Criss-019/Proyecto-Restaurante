package com.restaurante.ms_reservas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clienteId;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Integer cantidadPersonas;

    @Column(nullable = false, length = 50)
    private String estado; // Ej: PENDIENTE, CONFIRMADA, CANCELADA

    @Column(length = 255)
    private String observaciones;
}

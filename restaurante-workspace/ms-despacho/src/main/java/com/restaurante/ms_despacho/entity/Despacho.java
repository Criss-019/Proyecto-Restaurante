package com.restaurante.ms_despacho.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "despachos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica con el microservicio ms-pedidos
    @Column(nullable = false)
    private Long pedidoId;

    @Column(nullable = false, length = 100)
    private String repartidorAsignado;

    @Column(nullable = false, length = 255)
    private String direccionEntrega;

    // Estados logísticos: EN_PREPARACION, EN_RUTA, ENTREGADO, RECHAZADO
    @Column(nullable = false, length = 50)
    private String estado;

    @Column(name = "fecha_salida")
    private LocalDateTime fechaSalida;

    @Column(name = "fecha_entrega_estimada")
    private LocalDateTime fechaEntregaEstimada;

    @Column(name = "fecha_entrega")
    private LocalDateTime fechaEntrega;
}

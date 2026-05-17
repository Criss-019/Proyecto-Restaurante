package com.restaurante.ms_notificaciones.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica opcional para saber a qué cliente pertenece en el historial
    @Column(nullable = false)
    private Long clienteId;

    // Email o número de teléfono
    @Column(nullable = false, length = 150)
    private String destinatario;

    // Ej: EMAIL, SMS, PUSH
    @Column(nullable = false, length = 50)
    private String tipo;

    @Column(length = 150)
    private String asunto; // Para correos

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje; // Cuerpo del mensaje

    // Ej: PENDIENTE, ENVIADO, FALLIDO
    @Column(nullable = false, length = 50)
    private String estado;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    @Column
    private LocalDateTime fechaEnvio;
}


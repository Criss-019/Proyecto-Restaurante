package com.restaurante.ms_facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación lógica con el microservicio ms-pedidos
    @Column(nullable = false)
    private Long pedidoId;

    // Número de folio o documento fiscal oficial (Ej: FACT-000012)
    @Column(nullable = false, unique = true, length = 50)
    private String numeroFactura;

    @Column(nullable = false)
    private Double subtotal;

    @Column(nullable = false)
    private Double impuestos; // Monto calculado del IVA/Tasa

    @Column(nullable = false)
    private Double total;

    @Column(nullable = false)
    private LocalDateTime fechaEmision;

    // Estados fiscales comunes: EMITIDA, ANULADA, RECHAZADA_POR_ENTE
    @Column(nullable = false, length = 50)
    private String estadoFiscal;

    // Almacenará la ruta física o URL del documento (S3, disco local, etc.) una vez generado el archivo real
    @Column(length = 255)
    private String urlPdf;
}

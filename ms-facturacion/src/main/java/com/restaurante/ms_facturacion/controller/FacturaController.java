package com.restaurante.ms_facturacion.controller;

import com.restaurante.ms_facturacion.dto.FacturaRequestDTO;
import com.restaurante.ms_facturacion.dto.FacturaResponseDTO;
import com.restaurante.ms_facturacion.service.FacturaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturaController {

    private final FacturaService facturaService;

    @PostMapping
    public ResponseEntity<FacturaResponseDTO> emitir(@Valid @RequestBody FacturaRequestDTO request) {
        log.info("Petición REST recibida para emitir comprobante fiscal");
        return new ResponseEntity<>(facturaService.emitirFactura(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FacturaResponseDTO>> listar() {
        log.info("Petición REST recibida para listar documentos tributarios");
        return ResponseEntity.ok(facturaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener comprobante por ID");
        return ResponseEntity.ok(facturaService.obtenerPorId(id));
    }

    // Endpoint crucial para que ms-pedidos o el cliente recuperen su boleta a partir de la compra
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<FacturaResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        log.info("Petición REST recibida para obtener comprobante por Pedido ID");
        return ResponseEntity.ok(facturaService.obtenerPorPedidoId(pedidoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FacturaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody FacturaRequestDTO request) {
        log.info("Petición REST recibida para actualizar datos base de facturación");
        return ResponseEntity.ok(facturaService.actualizarFactura(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<FacturaResponseDTO> cambiarEstadoFiscal(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado legal de la factura");
        return ResponseEntity.ok(facturaService.cambiarEstadoFiscal(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar registro log de factura");
        facturaService.eliminarFactura(id);
        return ResponseEntity.noContent().build();
    }
}

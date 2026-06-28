package com.restaurante.ms_pagos.controller;

import com.restaurante.ms_pagos.dto.PagoRequestDTO;
import com.restaurante.ms_pagos.dto.PagoResponseDTO;
import com.restaurante.ms_pagos.service.PagoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<PagoResponseDTO> registrar(@Valid @RequestBody PagoRequestDTO request) {
        log.info("Petición REST recibida para registrar pago");
        return new ResponseEntity<>(pagoService.registrarPago(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PagoResponseDTO>> listar() {
        log.info("Petición REST recibida para listar pagos");
        return ResponseEntity.ok(pagoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener pago por ID");
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    // Endpoint para buscar todos los pagos asociados a un pedido específico
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<PagoResponseDTO>> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        log.info("Petición REST recibida para listar pagos por Pedido ID");
        return ResponseEntity.ok(pagoService.obtenerPorPedidoId(pedidoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PagoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PagoRequestDTO request) {
        log.info("Petición REST recibida para actualizar pago");
        return ResponseEntity.ok(pagoService.actualizarPago(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PagoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de pago");
        return ResponseEntity.ok(pagoService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar pago");
        pagoService.eliminarPago(id);
        return ResponseEntity.noContent().build();
    }
}


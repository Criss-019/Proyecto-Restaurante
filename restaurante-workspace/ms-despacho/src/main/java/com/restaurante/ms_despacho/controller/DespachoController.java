package com.restaurante.ms_despacho.controller;

import com.restaurante.ms_despacho.dto.DespachoRequestDTO;
import com.restaurante.ms_despacho.dto.DespachoResponseDTO;
import com.restaurante.ms_despacho.service.DespachoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private final DespachoService despachoService;

    @PostMapping
    public ResponseEntity<DespachoResponseDTO> programar(@Valid @RequestBody DespachoRequestDTO request) {
        log.info("Petición REST recibida para programar despacho");
        return new ResponseEntity<>(despachoService.programarDespacho(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<DespachoResponseDTO>> listar() {
        log.info("Petición REST recibida para listar despachos");
        return ResponseEntity.ok(despachoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespachoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener despacho por ID");
        return ResponseEntity.ok(despachoService.obtenerPorId(id));
    }

    // Endpoint clave para saber el estado logístico a partir de un Pedido sin conocer el ID del despacho
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<DespachoResponseDTO> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        log.info("Petición REST recibida para obtener despacho por Pedido ID");
        return ResponseEntity.ok(despachoService.obtenerPorPedidoId(pedidoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DespachoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody DespachoRequestDTO request) {
        log.info("Petición REST recibida para actualizar despacho");
        return ResponseEntity.ok(despachoService.actualizarDespacho(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<DespachoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de despacho");
        return ResponseEntity.ok(despachoService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar despacho");
        despachoService.eliminarDespacho(id);
        return ResponseEntity.noContent().build();
    }
}

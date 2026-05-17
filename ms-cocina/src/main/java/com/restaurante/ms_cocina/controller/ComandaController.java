package com.restaurante.ms_cocina.controller;

import com.restaurante.ms_cocina.dto.ComandaRequestDTO;
import com.restaurante.ms_cocina.dto.ComandaResponseDTO;
import com.restaurante.ms_cocina.service.ComandaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cocina/comandas")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService comandaService;

    @PostMapping
    public ResponseEntity<ComandaResponseDTO> crear(@Valid @RequestBody ComandaRequestDTO request) {
        log.info("Petición REST recibida para crear comanda");
        return new ResponseEntity<>(comandaService.crearComanda(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ComandaResponseDTO>> listar() {
        log.info("Petición REST recibida para listar comandas");
        return ResponseEntity.ok(comandaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComandaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener comanda por ID");
        return ResponseEntity.ok(comandaService.obtenerPorId(id));
    }

    // Endpoint adicional para buscar comandas por Pedido
    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<ComandaResponseDTO>> obtenerPorPedidoId(@PathVariable Long pedidoId) {
        log.info("Petición REST recibida para listar comandas por Pedido ID");
        return ResponseEntity.ok(comandaService.obtenerPorPedidoId(pedidoId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComandaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ComandaRequestDTO request) {
        log.info("Petición REST recibida para actualizar comanda");
        return ResponseEntity.ok(comandaService.actualizarComanda(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ComandaResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de comanda");
        return ResponseEntity.ok(comandaService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar comanda");
        comandaService.eliminarComanda(id);
        return ResponseEntity.noContent().build();
    }
}


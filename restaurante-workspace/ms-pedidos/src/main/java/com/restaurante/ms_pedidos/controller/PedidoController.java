package com.restaurante.ms_pedidos.controller;

import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import com.restaurante.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crear(@Valid @RequestBody PedidoRequestDTO request) {
        log.info("Petición REST recibida para crear pedido");
        return new ResponseEntity<>(pedidoService.crearPedido(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> listar() {
        log.info("Petición REST recibida para listar pedidos");
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener pedido por ID");
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    // Endpoint para buscar todos los pedidos de un cliente específico
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPorClienteId(@PathVariable Long clienteId) {
        log.info("Petición REST recibida para listar pedidos por Cliente ID");
        return ResponseEntity.ok(pedidoService.obtenerPorClienteId(clienteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody PedidoRequestDTO request) {
        log.info("Petición REST recibida para actualizar pedido");
        return ResponseEntity.ok(pedidoService.actualizarPedido(id, request));
    }

    // Cambio de @PatchMapping a @PutMapping
    @PutMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de pedido");
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar pedido");
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
}


package com.restaurante.ms_clientes.controller;

import com.restaurante.ms_clientes.dto.ClienteRequestDTO;
import com.restaurante.ms_clientes.dto.ClienteResponseDTO;
import com.restaurante.ms_clientes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteRequestDTO request) {
        log.info("Petición REST recibida para crear cliente");
        return new ResponseEntity<>(clienteService.crearCliente(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listar() {
        log.info("Petición REST recibida para listar clientes");
        return ResponseEntity.ok(clienteService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener cliente por ID");
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO request) {
        log.info("Petición REST recibida para actualizar cliente");
        return ResponseEntity.ok(clienteService.actualizarCliente(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar cliente");
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}

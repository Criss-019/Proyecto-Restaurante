package com.restaurante.ms_stock.controller;

import com.restaurante.ms_stock.dto.IngredienteRequestDTO;
import com.restaurante.ms_stock.dto.IngredienteResponseDTO;
import com.restaurante.ms_stock.service.IngredienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stock/ingredientes")
@RequiredArgsConstructor
public class IngredienteController {

    private final IngredienteService service;

    @PostMapping
    public ResponseEntity<IngredienteResponseDTO> crear(@Valid @RequestBody IngredienteRequestDTO request) {
        log.info("Petición REST: Crear ingrediente");
        return new ResponseEntity<>(service.crear(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<IngredienteResponseDTO>> listar() {
        log.info("Petición REST: Listar ingredientes");
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredienteResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST: Obtener ingrediente por ID");
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IngredienteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody IngredienteRequestDTO request) {
        log.info("Petición REST: Actualizar ingrediente");
        return ResponseEntity.ok(service.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST: Eliminar ingrediente");
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

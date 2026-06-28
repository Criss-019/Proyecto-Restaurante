package com.restaurante.ms_catalogo.controller;

import com.restaurante.ms_catalogo.dto.PlatoRequestDTO;
import com.restaurante.ms_catalogo.dto.PlatoResponseDTO;
import com.restaurante.ms_catalogo.service.PlatoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/catalogo/platos")
@RequiredArgsConstructor
public class PlatoController {

    private final PlatoService platoService;

    @PostMapping
    public ResponseEntity<PlatoResponseDTO> crearPlato(@Valid @RequestBody PlatoRequestDTO request) {
        log.info("Petición REST recibida para crear plato");
        return new ResponseEntity<>(platoService.crearPlato(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PlatoResponseDTO>> obtenerTodos() {
        log.info("Petición REST recibida para listar platos");
        return ResponseEntity.ok(platoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener plato por ID");
        return ResponseEntity.ok(platoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlatoResponseDTO> actualizarPlato(@PathVariable Long id, @Valid @RequestBody PlatoRequestDTO request) {
        log.info("Petición REST recibida para actualizar plato");
        return ResponseEntity.ok(platoService.actualizarPlato(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPlato(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar plato");
        platoService.eliminarPlato(id);
        return ResponseEntity.noContent().build();
    }
}


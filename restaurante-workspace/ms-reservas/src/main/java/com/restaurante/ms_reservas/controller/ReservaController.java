package com.restaurante.ms_reservas.controller;

import com.restaurante.ms_reservas.dto.ReservaRequestDTO;
import com.restaurante.ms_reservas.dto.ReservaResponseDTO;
import com.restaurante.ms_reservas.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> crear(@Valid @RequestBody ReservaRequestDTO request) {
        log.info("Petición REST recibida para crear reserva");
        return new ResponseEntity<>(reservaService.crearReserva(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ReservaResponseDTO>> listar() {
        log.info("Petición REST recibida para listar reservas");
        return ResponseEntity.ok(reservaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener reserva por ID");
        return ResponseEntity.ok(reservaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ReservaRequestDTO request) {
        log.info("Petición REST recibida para actualizar reserva");
        return ResponseEntity.ok(reservaService.actualizarReserva(id, request));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReservaResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de reserva");
        return ResponseEntity.ok(reservaService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar reserva");
        reservaService.eliminarReserva(id);
        return ResponseEntity.noContent().build();
    }
}

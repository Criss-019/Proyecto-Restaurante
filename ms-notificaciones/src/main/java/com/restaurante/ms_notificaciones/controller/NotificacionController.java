package com.restaurante.ms_notificaciones.controller;

import com.restaurante.ms_notificaciones.dto.NotificacionRequestDTO;
import com.restaurante.ms_notificaciones.dto.NotificacionResponseDTO;
import com.restaurante.ms_notificaciones.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/notificaciones")
@RequiredArgsConstructor
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<NotificacionResponseDTO> enviar(@Valid @RequestBody NotificacionRequestDTO request) {
        log.info("Petición REST recibida para crear y enviar notificación");
        return new ResponseEntity<>(notificacionService.crearYEnviarNotificacion(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>> listar() {
        log.info("Petición REST recibida para listar notificaciones");
        return ResponseEntity.ok(notificacionService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificacionResponseDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Petición REST recibida para obtener notificación por ID");
        return ResponseEntity.ok(notificacionService.obtenerPorId(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<NotificacionResponseDTO>> obtenerPorClienteId(@PathVariable Long clienteId) {
        log.info("Petición REST recibida para listar notificaciones por Cliente ID");
        return ResponseEntity.ok(notificacionService.obtenerPorClienteId(clienteId));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<NotificacionResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        log.info("Petición REST recibida para cambiar estado de notificación");
        return ResponseEntity.ok(notificacionService.cambiarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        log.info("Petición REST recibida para eliminar notificación");
        notificacionService.eliminarNotificacion(id);
        return ResponseEntity.noContent().build();
    }
}


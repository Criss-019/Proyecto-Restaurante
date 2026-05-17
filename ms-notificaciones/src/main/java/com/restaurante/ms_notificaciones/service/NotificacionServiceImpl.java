package com.restaurante.ms_notificaciones.service;

import com.restaurante.ms_notificaciones.dto.NotificacionRequestDTO;
import com.restaurante.ms_notificaciones.dto.NotificacionResponseDTO;
import com.restaurante.ms_notificaciones.entity.Notificacion;
import com.restaurante.ms_notificaciones.exception.ResourceNotFoundException;
import com.restaurante.ms_notificaciones.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Override
    public NotificacionResponseDTO crearYEnviarNotificacion(NotificacionRequestDTO request) {
        log.info("Preparando envío de {} a {}", request.getTipo(), request.getDestinatario());
        
        // Fase 1: Creación del registro
        Notificacion notificacion = Notificacion.builder()
                .clienteId(request.getClienteId())
                .destinatario(request.getDestinatario())
                .tipo(request.getTipo())
                .asunto(request.getAsunto())
                .mensaje(request.getMensaje())
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .build();
        
        Notificacion guardada = notificacionRepository.save(notificacion);
        
        // Fase 2: Simulación del envío real
        // Aquí en el futuro se usaría JavaMailSender, SendGrid o Twilio.
        // Para la arquitectura actual, simulamos que el envío fue exitoso inmediatamente.
        try {
            log.info("Simulando conexión con el proveedor (Email/SMS)...");
            guardada.setEstado("ENVIADO");
            guardada.setFechaEnvio(LocalDateTime.now());
            notificacionRepository.save(guardada);
            log.info("Notificación enviada exitosamente al destinatario");
        } catch (Exception e) {
            log.error("Fallo al enviar la notificación: {}", e.getMessage());
            guardada.setEstado("FALLIDO");
            notificacionRepository.save(guardada);
        }

        return mapToDTO(guardada);
    }

    @Override
    public List<NotificacionResponseDTO> obtenerTodas() {
        log.info("Consultando historial completo de notificaciones");
        return notificacionRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificacionResponseDTO obtenerPorId(Long id) {
        log.info("Consultando notificación con ID: {}", id);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + id));
        return mapToDTO(notificacion);
    }

    @Override
    public List<NotificacionResponseDTO> obtenerPorClienteId(Long clienteId) {
        log.info("Consultando historial de notificaciones para el cliente ID: {}", clienteId);
        return notificacionRepository.findByClienteId(clienteId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificacionResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        // Útil si hay un sistema externo de webhooks que nos avisa que el correo rebotó (bounced)
        log.info("Actualizando estado de notificación ID: {} a {}", id, nuevoEstado);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + id));
        
        notificacion.setEstado(nuevoEstado);
        Notificacion actualizada = notificacionRepository.save(notificacion);
        return mapToDTO(actualizada);
    }

    @Override
    public void eliminarNotificacion(Long id) {
        log.info("Eliminando registro de notificación con ID: {}", id);
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada con ID: " + id));
        notificacionRepository.delete(notificacion);
    }

    private NotificacionResponseDTO mapToDTO(Notificacion notificacion) {
        return NotificacionResponseDTO.builder()
                .id(notificacion.getId())
                .clienteId(notificacion.getClienteId())
                .destinatario(notificacion.getDestinatario())
                .tipo(notificacion.getTipo())
                .asunto(notificacion.getAsunto())
                .mensaje(notificacion.getMensaje())
                .estado(notificacion.getEstado())
                .fechaCreacion(notificacion.getFechaCreacion())
                .fechaEnvio(notificacion.getFechaEnvio())
                .build();
    }
}


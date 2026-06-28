package com.restaurante.ms_notificaciones.service;

import com.restaurante.ms_notificaciones.dto.NotificacionRequestDTO;
import com.restaurante.ms_notificaciones.dto.NotificacionResponseDTO;
import java.util.List;

public interface NotificacionService {
    NotificacionResponseDTO crearYEnviarNotificacion(NotificacionRequestDTO request);
    List<NotificacionResponseDTO> obtenerTodas();
    NotificacionResponseDTO obtenerPorId(Long id);
    List<NotificacionResponseDTO> obtenerPorClienteId(Long clienteId);
    NotificacionResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarNotificacion(Long id);
}


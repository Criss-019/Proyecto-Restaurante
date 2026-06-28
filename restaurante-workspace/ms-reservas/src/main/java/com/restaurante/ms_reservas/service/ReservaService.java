package com.restaurante.ms_reservas.service;

import com.restaurante.ms_reservas.dto.ReservaRequestDTO;
import com.restaurante.ms_reservas.dto.ReservaResponseDTO;
import java.util.List;

public interface ReservaService {
    ReservaResponseDTO crearReserva(ReservaRequestDTO request);
    List<ReservaResponseDTO> obtenerTodas();
    ReservaResponseDTO obtenerPorId(Long id);
    ReservaResponseDTO actualizarReserva(Long id, ReservaRequestDTO request);
    ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarReserva(Long id);
}

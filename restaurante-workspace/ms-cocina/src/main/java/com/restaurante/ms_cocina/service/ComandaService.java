package com.restaurante.ms_cocina.service;

import com.restaurante.ms_cocina.dto.ComandaRequestDTO;
import com.restaurante.ms_cocina.dto.ComandaResponseDTO;
import java.util.List;

public interface ComandaService {
    ComandaResponseDTO crearComanda(ComandaRequestDTO request);
    List<ComandaResponseDTO> obtenerTodas();
    ComandaResponseDTO obtenerPorId(Long id);
    // Agregamos un método específico para buscar por PedidoId
    List<ComandaResponseDTO> obtenerPorPedidoId(Long pedidoId); 
    ComandaResponseDTO actualizarComanda(Long id, ComandaRequestDTO request);
    ComandaResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarComanda(Long id);
}

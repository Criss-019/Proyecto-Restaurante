package com.restaurante.ms_despacho.service;

import com.restaurante.ms_despacho.dto.DespachoRequestDTO;
import com.restaurante.ms_despacho.dto.DespachoResponseDTO;
import java.util.List;

public interface DespachoService {
    DespachoResponseDTO programarDespacho(DespachoRequestDTO request);
    List<DespachoResponseDTO> obtenerTodos();
    DespachoResponseDTO obtenerPorId(Long id);
    DespachoResponseDTO obtenerPorPedidoId(Long pedidoId);
    DespachoResponseDTO actualizarDespacho(Long id, DespachoRequestDTO request);
    DespachoResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarDespacho(Long id);
}

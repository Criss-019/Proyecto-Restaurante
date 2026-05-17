package com.restaurante.ms_pagos.service;

import com.restaurante.ms_pagos.dto.PagoRequestDTO;
import com.restaurante.ms_pagos.dto.PagoResponseDTO;
import java.util.List;

public interface PagoService {
    PagoResponseDTO registrarPago(PagoRequestDTO request);
    List<PagoResponseDTO> obtenerTodos();
    PagoResponseDTO obtenerPorId(Long id);
    List<PagoResponseDTO> obtenerPorPedidoId(Long pedidoId); // Método estratégico
    PagoResponseDTO actualizarPago(Long id, PagoRequestDTO request);
    PagoResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarPago(Long id);
}


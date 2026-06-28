package com.restaurante.ms_facturacion.service;

import com.restaurante.ms_facturacion.dto.FacturaRequestDTO;
import com.restaurante.ms_facturacion.dto.FacturaResponseDTO;
import java.util.List;

public interface FacturaService {
    FacturaResponseDTO emitirFactura(FacturaRequestDTO request);
    List<FacturaResponseDTO> obtenerTodas();
    FacturaResponseDTO obtenerPorId(Long id);
    FacturaResponseDTO obtenerPorPedidoId(Long pedidoId); // Requerido para el ciclo de vida del pedido
    FacturaResponseDTO actualizarFactura(Long id, FacturaRequestDTO request);
    FacturaResponseDTO cambiarEstadoFiscal(Long id, String nuevoEstado);
    void eliminarFactura(Long id);
}

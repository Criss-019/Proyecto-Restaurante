package com.restaurante.ms_pedidos.service;

import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crearPedido(PedidoRequestDTO request);
    List<PedidoResponseDTO> obtenerTodos();
    PedidoResponseDTO obtenerPorId(Long id);
    List<PedidoResponseDTO> obtenerPorClienteId(Long clienteId); // Nuevo método estratégico
    PedidoResponseDTO actualizarPedido(Long id, PedidoRequestDTO request);
    PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado);
    void eliminarPedido(Long id);
}

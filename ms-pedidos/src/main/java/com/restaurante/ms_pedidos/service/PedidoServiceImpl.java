package com.restaurante.ms_pedidos.service;

import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import com.restaurante.ms_pedidos.entity.Pedido;
import com.restaurante.ms_pedidos.exception.ResourceNotFoundException;
import com.restaurante.ms_pedidos.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;

    @Override
    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {
        log.info("Iniciando creación de pedido para el cliente ID: {}", request.getClienteId());
        
        // Más adelante, aquí usaremos Feign para consultar a ms-clientes si el ID realmente existe
        
        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .fechaPedido(LocalDateTime.now()) // El sistema asigna la hora exacta
                .tipoEntrega(request.getTipoEntrega())
                .estado("CREADO") // Estado inicial por defecto
                .total(0.0) // Inicia en 0, luego se sumarán los platos
                .build();
        
        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado exitosamente con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Override
    public List<PedidoResponseDTO> obtenerTodos() {
        log.info("Consultando todos los pedidos");
        return pedidoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoResponseDTO obtenerPorId(Long id) {
        log.info("Consultando pedido con ID: {}", id);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        return mapToDTO(pedido);
    }

    @Override
    public List<PedidoResponseDTO> obtenerPorClienteId(Long clienteId) {
        log.info("Consultando historial de pedidos para el cliente ID: {}", clienteId);
        return pedidoRepository.findByClienteId(clienteId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PedidoResponseDTO actualizarPedido(Long id, PedidoRequestDTO request) {
        log.info("Actualizando pedido con ID: {}", id);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        
        pedido.setClienteId(request.getClienteId());
        pedido.setTipoEntrega(request.getTipoEntrega());
        
        Pedido actualizado = pedidoRepository.save(pedido);
        log.info("Pedido con ID: {} actualizado exitosamente", id);
        return mapToDTO(actualizado);
    }

    @Override
    public PedidoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado del pedido ID: {} a {}", id, nuevoEstado);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        
        pedido.setEstado(nuevoEstado);
        Pedido actualizado = pedidoRepository.save(pedido);
        return mapToDTO(actualizado);
    }

    @Override
    public void eliminarPedido(Long id) {
        log.info("Eliminando pedido con ID: {}", id);
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        pedidoRepository.delete(pedido);
        log.info("Pedido con ID: {} eliminado exitosamente", id);
    }

    private PedidoResponseDTO mapToDTO(Pedido pedido) {
        return PedidoResponseDTO.builder()
                .id(pedido.getId())
                .clienteId(pedido.getClienteId())
                .fechaPedido(pedido.getFechaPedido())
                .tipoEntrega(pedido.getTipoEntrega())
                .estado(pedido.getEstado())
                .total(pedido.getTotal())
                .build();
    }
}


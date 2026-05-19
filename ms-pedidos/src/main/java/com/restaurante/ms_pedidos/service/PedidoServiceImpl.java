package com.restaurante.ms_pedidos.service;

import com.restaurante.ms_pedidos.client.ClienteClient;
import com.restaurante.ms_pedidos.client.dto.ClienteDTO;
import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import com.restaurante.ms_pedidos.entity.Pedido;
import com.restaurante.ms_pedidos.exception.ResourceNotFoundException;
import com.restaurante.ms_pedidos.repository.PedidoRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.restaurante.ms_pedidos.client.NotificacionClient;
import com.restaurante.ms_pedidos.client.dto.NotificacionRequestDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;

    // Inyectamos Feign Client
    private final ClienteClient clienteClient;

    private final NotificacionClient notificacionClient;

    @Override
    public PedidoResponseDTO crearPedido(PedidoRequestDTO request) {
        log.info("Iniciando creación de pedido para el cliente ID: {}", request.getClienteId());
        
        ClienteDTO cliente = null; // Lo declaramos fuera del try para poder usar su email después
        
        try {
            log.info("Consultando a ms-clientes para verificar la existencia del cliente...");
            cliente = clienteClient.obtenerPorId(request.getClienteId());
            log.info("Cliente verificado con éxito: {}", cliente.getNombre());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("No se puede crear el pedido: El cliente con ID " + request.getClienteId() + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("Error interno al verificar el cliente.");
        }
        
        // Creamos y guardamos el pedido
        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega(request.getTipoEntrega())
                .estado("CREADO")
                .total(0.0)
                .build();
        
        Pedido guardado = pedidoRepository.save(pedido);
        log.info("Pedido creado exitosamente con ID: {}", guardado.getId());

        // --- NUEVA LÓGICA: Enviar Notificación al Cliente ---
        if (cliente != null && cliente.getEmail() != null) {
            try {
                log.info("Enviando notificación de confirmación al correo: {}", cliente.getEmail());
                NotificacionRequestDTO notificacion = NotificacionRequestDTO.builder()
                        .destinatario(cliente.getEmail())
                        .asunto("¡Pedido Recibido!")
                        .mensaje("Hola " + cliente.getNombre() + ", hemos recibido tu pedido #" + guardado.getId() + ". Lo estamos preparando.")
                        .tipo("EMAIL")
                        .build();
                        
                notificacionClient.enviarNotificacion(notificacion);
                log.info("Notificación enviada con éxito.");
            } catch (FeignException e) {
                // Si la notificación falla, NO cancelamos el pedido. Solo registramos el error.
                log.error("El pedido se creó, pero falló el envío de la notificación: {}", e.getMessage());
            }
        }
        // ----------------------------------------------------

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


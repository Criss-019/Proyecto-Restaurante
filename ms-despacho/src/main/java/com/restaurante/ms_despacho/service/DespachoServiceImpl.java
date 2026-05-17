package com.restaurante.ms_despacho.service;

import com.restaurante.ms_despacho.dto.DespachoRequestDTO;
import com.restaurante.ms_despacho.dto.DespachoResponseDTO;
import com.restaurante.ms_despacho.entity.Despacho;
import com.restaurante.ms_despacho.exception.ResourceNotFoundException;
import com.restaurante.ms_despacho.repository.DespachoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DespachoServiceImpl implements DespachoService {

    private final DespachoRepository despachoRepository;

    @Override
    public DespachoResponseDTO programarDespacho(DespachoRequestDTO request) {
        log.info("Iniciando programación de despacho para el pedido ID: {}", request.getPedidoId());
        
        // Todo despacho inicia en preparación dentro de la sucursal antes de salir a la ruta
        Despacho despacho = Despacho.builder()
                .pedidoId(request.getPedidoId())
                .repartidorAsignado(request.getRepartidorAsignado())
                .direccionEntrega(request.getDireccionEntrega())
                .estado("EN_PREPARACION") 
                .fechaSalida(null) // Aún no ha salido el repartidor
                .fechaEntregaEstimada(request.getFechaEntregaEstimada())
                .build();
        
        Despacho guardado = despachoRepository.save(despacho);
        log.info("Logística de despacho guardada exitosamente con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Override
    public List<DespachoResponseDTO> obtenerTodos() {
        log.info("Consultando todos los despachos");
        return despachoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DespachoResponseDTO obtenerPorId(Long id) {
        log.info("Consultando despacho con ID: {}", id);
        Despacho despacho = despachoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + id));
        return mapToDTO(despacho);
    }

    @Override
    public DespachoResponseDTO obtenerPorPedidoId(Long pedidoId) {
        log.info("Consultando despacho asociado al pedido ID: {}", pedidoId);
        Despacho despacho = despachoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró logística de despacho para el pedido ID: " + pedidoId));
        return mapToDTO(despacho);
    }

    @Override
    public DespachoResponseDTO actualizarDespacho(Long id, DespachoRequestDTO request) {
        log.info("Actualizando despacho con ID: {}", id);
        Despacho despacho = despachoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + id));
        
        despacho.setPedidoId(request.getPedidoId());
        despacho.setRepartidorAsignado(request.getRepartidorAsignado());
        despacho.setDireccionEntrega(request.getDireccionEntrega());
        despacho.setFechaEntregaEstimada(request.getFechaEntregaEstimada());
        
        Despacho actualizado = despachoRepository.save(despacho);
        log.info("Despacho con ID: {} actualizado con éxito", id);
        return mapToDTO(actualizado);
    }

    @Override
    public DespachoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado del despacho ID: {} a {}", id, nuevoEstado);
        Despacho despacho = despachoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + id));
        
        despacho.setEstado(nuevoEstado);
        
        // Lógica incremental: si el estado cambia a EN_RUTA, marcamos el instante exacto de salida
        if ("EN_RUTA".equalsIgnoreCase(nuevoEstado)) {
            despacho.setFechaSalida(LocalDateTime.now());
        }
        
        Despacho actualizado = despachoRepository.save(despacho);
        return mapToDTO(actualizado);
    }

    @Override
    public void eliminarDespacho(Long id) {
        log.info("Eliminando despacho con ID: {}", id);
        Despacho despacho = despachoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Despacho no encontrado con ID: " + id));
        despachoRepository.delete(despacho);
        log.info("Despacho con ID: {} eliminado exitosamente", id);
    }

    private DespachoResponseDTO mapToDTO(Despacho despacho) {
        return DespachoResponseDTO.builder()
                .id(despacho.getId())
                .pedidoId(despacho.getPedidoId())
                .repartidorAsignado(despacho.getRepartidorAsignado())
                .direccionEntrega(despacho.getDireccionEntrega())
                .estado(despacho.getEstado())
                .fechaSalida(despacho.getFechaSalida())
                .fechaEntregaEstimada(despacho.getFechaEntregaEstimada())
                .build();
    }
}

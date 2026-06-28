package com.restaurante.ms_cocina.service;

import com.restaurante.ms_cocina.dto.ComandaRequestDTO;
import com.restaurante.ms_cocina.dto.ComandaResponseDTO;
import com.restaurante.ms_cocina.entity.Comanda;
import com.restaurante.ms_cocina.exception.ResourceNotFoundException;
import com.restaurante.ms_cocina.repository.ComandaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComandaServiceImpl implements ComandaService {

    private final ComandaRepository comandaRepository;

    @Override
    public ComandaResponseDTO crearComanda(ComandaRequestDTO request) {
        log.info("Iniciando creación de comanda para el pedido ID: {}", request.getPedidoId());
        
        Comanda comanda = Comanda.builder()
                .pedidoId(request.getPedidoId())
                .platoId(request.getPlatoId())
                .cantidad(request.getCantidad())
                .estado("PENDIENTE") // Toda nueva comanda inicia en PENDIENTE
                .notas(request.getNotas())
                .build();
        
        Comanda guardada = comandaRepository.save(comanda);
        log.info("Comanda creada exitosamente con ID: {}", guardada.getId());
        return mapToDTO(guardada);
    }

    @Override
    public List<ComandaResponseDTO> obtenerTodas() {
        log.info("Consultando todas las comandas");
        return comandaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComandaResponseDTO obtenerPorId(Long id) {
        log.info("Consultando comanda con ID: {}", id);
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda no encontrada con ID: " + id));
        return mapToDTO(comanda);
    }

    @Override
    public List<ComandaResponseDTO> obtenerPorPedidoId(Long pedidoId) {
        log.info("Consultando comandas asociadas al pedido ID: {}", pedidoId);
        // Este método será vital cuando ms-pedidos quiera saber si la cocina ya terminó la orden
        return comandaRepository.findByPedidoId(pedidoId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ComandaResponseDTO actualizarComanda(Long id, ComandaRequestDTO request) {
        log.info("Actualizando comanda con ID: {}", id);
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda no encontrada con ID: " + id));
        
        comanda.setPedidoId(request.getPedidoId());
        comanda.setPlatoId(request.getPlatoId());
        comanda.setCantidad(request.getCantidad());
        comanda.setNotas(request.getNotas());
        
        Comanda actualizada = comandaRepository.save(comanda);
        log.info("Comanda con ID: {} actualizada exitosamente", id);
        return mapToDTO(actualizada);
    }

    @Override
    public ComandaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado de la comanda ID: {} a {}", id, nuevoEstado);
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda no encontrada con ID: " + id));
        
        // Más adelante, aquí podríamos llamar a ms-stock con Feign si el estado cambia a "EN_PREPARACION" 
        // para descontar los ingredientes correspondientes.
        
        comanda.setEstado(nuevoEstado);
        Comanda actualizada = comandaRepository.save(comanda);
        return mapToDTO(actualizada);
    }

    @Override
    public void eliminarComanda(Long id) {
        log.info("Eliminando comanda con ID: {}", id);
        Comanda comanda = comandaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comanda no encontrada con ID: " + id));
        comandaRepository.delete(comanda);
        log.info("Comanda con ID: {} eliminada exitosamente", id);
    }

    private ComandaResponseDTO mapToDTO(Comanda comanda) {
        return ComandaResponseDTO.builder()
                .id(comanda.getId())
                .pedidoId(comanda.getPedidoId())
                .platoId(comanda.getPlatoId())
                .cantidad(comanda.getCantidad())
                .estado(comanda.getEstado())
                .notas(comanda.getNotas())
                .build();
    }
}


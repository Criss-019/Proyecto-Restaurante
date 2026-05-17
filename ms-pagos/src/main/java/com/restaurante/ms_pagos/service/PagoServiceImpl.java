package com.restaurante.ms_pagos.service;

import com.restaurante.ms_pagos.dto.PagoRequestDTO;
import com.restaurante.ms_pagos.dto.PagoResponseDTO;
import com.restaurante.ms_pagos.entity.Pago;
import com.restaurante.ms_pagos.exception.ResourceNotFoundException;
import com.restaurante.ms_pagos.repository.PagoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    @Override
    public PagoResponseDTO registrarPago(PagoRequestDTO request) {
        log.info("Iniciando registro de pago para el pedido ID: {}", request.getPedidoId());
        
        // El sistema toma la fecha actual y asume APROBADO como estado inicial en este flujo feliz
        Pago pago = Pago.builder()
                .pedidoId(request.getPedidoId())
                .monto(request.getMonto())
                .metodoPago(request.getMetodoPago())
                .fechaPago(LocalDateTime.now()) 
                .estado("APROBADO") 
                .build();
        
        Pago guardado = pagoRepository.save(pago);
        log.info("Pago registrado exitosamente con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Override
    public List<PagoResponseDTO> obtenerTodos() {
        log.info("Consultando todos los pagos");
        return pagoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagoResponseDTO obtenerPorId(Long id) {
        log.info("Consultando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        return mapToDTO(pago);
    }

    @Override
    public List<PagoResponseDTO> obtenerPorPedidoId(Long pedidoId) {
        log.info("Consultando historial de pagos para el pedido ID: {}", pedidoId);
        return pagoRepository.findByPedidoId(pedidoId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PagoResponseDTO actualizarPago(Long id, PagoRequestDTO request) {
        log.info("Actualizando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        
        // Se actualizan los datos, pero la fecha de pago original se mantiene intacta por seguridad contable
        pago.setPedidoId(request.getPedidoId());
        pago.setMonto(request.getMonto());
        pago.setMetodoPago(request.getMetodoPago());
        
        Pago actualizado = pagoRepository.save(pago);
        log.info("Pago con ID: {} actualizado exitosamente", id);
        return mapToDTO(actualizado);
    }

    @Override
    public PagoResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado del pago ID: {} a {}", id, nuevoEstado);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        
        pago.setEstado(nuevoEstado);
        Pago actualizado = pagoRepository.save(pago);
        return mapToDTO(actualizado);
    }

    @Override
    public void eliminarPago(Long id) {
        log.info("Eliminando pago con ID: {}", id);
        Pago pago = pagoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        pagoRepository.delete(pago);
        log.info("Pago con ID: {} eliminado exitosamente", id);
    }

    private PagoResponseDTO mapToDTO(Pago pago) {
        return PagoResponseDTO.builder()
                .id(pago.getId())
                .pedidoId(pago.getPedidoId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .fechaPago(pago.getFechaPago())
                .estado(pago.getEstado())
                .build();
    }
}


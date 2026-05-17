package com.restaurante.ms_reservas.service;

import com.restaurante.ms_reservas.dto.ReservaRequestDTO;
import com.restaurante.ms_reservas.dto.ReservaResponseDTO;
import com.restaurante.ms_reservas.entity.Reserva;
import com.restaurante.ms_reservas.exception.ResourceNotFoundException;
import com.restaurante.ms_reservas.repository.ReservaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservaServiceImpl implements ReservaService {

    private final ReservaRepository reservaRepository;

    @Override
    public ReservaResponseDTO crearReserva(ReservaRequestDTO request) {
        log.info("Iniciando creación de reserva para el cliente ID: {}", request.getClienteId());
        
        Reserva reserva = Reserva.builder()
                .clienteId(request.getClienteId())
                .fechaHora(request.getFechaHora())
                .cantidadPersonas(request.getCantidadPersonas())
                .estado("CONFIRMADA") // Estado por defecto al crear
                .observaciones(request.getObservaciones())
                .build();
        
        Reserva guardada = reservaRepository.save(reserva);
        log.info("Reserva creada exitosamente con ID: {}", guardada.getId());
        return mapToDTO(guardada);
    }

    @Override
    public List<ReservaResponseDTO> obtenerTodas() {
        log.info("Consultando todas las reservas");
        return reservaRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReservaResponseDTO obtenerPorId(Long id) {
        log.info("Consultando reserva con ID: {}", id);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        return mapToDTO(reserva);
    }

    @Override
    public ReservaResponseDTO actualizarReserva(Long id, ReservaRequestDTO request) {
        log.info("Actualizando reserva con ID: {}", id);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        
        reserva.setClienteId(request.getClienteId());
        reserva.setFechaHora(request.getFechaHora());
        reserva.setCantidadPersonas(request.getCantidadPersonas());
        reserva.setObservaciones(request.getObservaciones());
        
        Reserva actualizada = reservaRepository.save(reserva);
        log.info("Reserva con ID: {} actualizada exitosamente", id);
        return mapToDTO(actualizada);
    }

    @Override
    public ReservaResponseDTO cambiarEstado(Long id, String nuevoEstado) {
        log.info("Cambiando estado de la reserva ID: {} a {}", id, nuevoEstado);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        
        reserva.setEstado(nuevoEstado);
        Reserva actualizada = reservaRepository.save(reserva);
        return mapToDTO(actualizada);
    }

    @Override
    public void eliminarReserva(Long id) {
        log.info("Eliminando reserva con ID: {}", id);
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con ID: " + id));
        reservaRepository.delete(reserva);
        log.info("Reserva con ID: {} eliminada exitosamente", id);
    }

    private ReservaResponseDTO mapToDTO(Reserva reserva) {
        return ReservaResponseDTO.builder()
                .id(reserva.getId())
                .clienteId(reserva.getClienteId())
                .fechaHora(reserva.getFechaHora())
                .cantidadPersonas(reserva.getCantidadPersonas())
                .estado(reserva.getEstado())
                .observaciones(reserva.getObservaciones())
                .build();
    }
}

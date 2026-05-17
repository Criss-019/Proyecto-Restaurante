package com.restaurante.ms_catalogo.service;

import com.restaurante.ms_catalogo.dto.PlatoRequestDTO;
import com.restaurante.ms_catalogo.dto.PlatoResponseDTO;
import com.restaurante.ms_catalogo.entity.Plato;
import com.restaurante.ms_catalogo.exception.ResourceNotFoundException;
import com.restaurante.ms_catalogo.repository.PlatoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlatoServiceImpl implements PlatoService {

    private final PlatoRepository platoRepository;

    @Override
    public PlatoResponseDTO crearPlato(PlatoRequestDTO request) {
        log.info("Iniciando creación de plato con nombre: {}", request.getNombre());
        Plato plato = Plato.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .precio(request.getPrecio())
                .disponible(request.getDisponible())
                .build();
        
        Plato platoGuardado = platoRepository.save(plato);
        log.info("Plato creado exitosamente con ID: {}", platoGuardado.getId());
        return mapToDTO(platoGuardado);
    }

    @Override
    public List<PlatoResponseDTO> obtenerTodos() {
        log.info("Consultando todos los platos del catálogo");
        return platoRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PlatoResponseDTO obtenerPorId(Long id) {
        log.info("Consultando plato con ID: {}", id);
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado con ID: " + id));
        return mapToDTO(plato);
    }

    @Override
    public PlatoResponseDTO actualizarPlato(Long id, PlatoRequestDTO request) {
        log.info("Actualizando plato con ID: {}", id);
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado con ID: " + id));
        
        plato.setNombre(request.getNombre());
        plato.setDescripcion(request.getDescripcion());
        plato.setPrecio(request.getPrecio());
        plato.setDisponible(request.getDisponible());
        
        Plato platoActualizado = platoRepository.save(plato);
        log.info("Plato con ID: {} actualizado exitosamente", id);
        return mapToDTO(platoActualizado);
    }

    @Override
    public void eliminarPlato(Long id) {
        log.info("Eliminando plato con ID: {}", id);
        Plato plato = platoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado con ID: " + id));
        platoRepository.delete(plato);
        log.info("Plato con ID: {} eliminado exitosamente", id);
    }

    // Método auxiliar para mapeo
    private PlatoResponseDTO mapToDTO(Plato plato) {
        return PlatoResponseDTO.builder()
                .id(plato.getId())
                .nombre(plato.getNombre())
                .descripcion(plato.getDescripcion())
                .precio(plato.getPrecio())
                .disponible(plato.getDisponible())
                .build();
    }
}

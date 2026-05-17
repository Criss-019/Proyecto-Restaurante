package com.restaurante.ms_stock.service;

import com.restaurante.ms_stock.dto.IngredienteRequestDTO;
import com.restaurante.ms_stock.dto.IngredienteResponseDTO;
import com.restaurante.ms_stock.entity.Ingrediente;
import com.restaurante.ms_stock.exception.ResourceNotFoundException;
import com.restaurante.ms_stock.repository.IngredienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngredienteServiceImpl implements IngredienteService {

    private final IngredienteRepository repository;

    @Override
    public IngredienteResponseDTO crear(IngredienteRequestDTO request) {
        log.info("Guardando nuevo ingrediente: {}", request.getNombre());
        Ingrediente entidad = Ingrediente.builder()
                .nombre(request.getNombre())
                .cantidadActual(request.getCantidadActual())
                .cantidadMinima(request.getCantidadMinima())
                .unidadMedida(request.getUnidadMedida())
                .build();
        
        Ingrediente guardado = repository.save(entidad);
        log.info("Ingrediente creado con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Override
    public List<IngredienteResponseDTO> listar() {
        log.info("Consultando el inventario completo de ingredientes");
        return repository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public IngredienteResponseDTO obtenerPorId(Long id) {
        log.info("Buscando ingrediente con ID: {}", id);
        Ingrediente ingrediente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));
        return mapToDTO(ingrediente);
    }

    @Override
    public IngredienteResponseDTO actualizar(Long id, IngredienteRequestDTO request) {
        log.info("Actualizando ingrediente con ID: {}", id);
        Ingrediente ingrediente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));

        ingrediente.setNombre(request.getNombre());
        ingrediente.setCantidadActual(request.getCantidadActual());
        ingrediente.setCantidadMinima(request.getCantidadMinima());
        ingrediente.setUnidadMedida(request.getUnidadMedida());

        Ingrediente actualizado = repository.save(ingrediente);
        log.info("Ingrediente actualizado exitosamente");
        return mapToDTO(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando ingrediente con ID: {}", id);
        Ingrediente ingrediente = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ingrediente no encontrado con ID: " + id));
        
        repository.delete(ingrediente);
        log.info("Ingrediente eliminado exitosamente");
    }

    // Método auxiliar para no repetir código
    private IngredienteResponseDTO mapToDTO(Ingrediente e) {
        return IngredienteResponseDTO.builder()
                .id(e.getId())
                .nombre(e.getNombre())
                .cantidadActual(e.getCantidadActual())
                .cantidadMinima(e.getCantidadMinima())
                .unidadMedida(e.getUnidadMedida())
                .build();
    }
}


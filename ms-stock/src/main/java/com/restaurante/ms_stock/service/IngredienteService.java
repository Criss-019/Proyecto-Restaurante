package com.restaurante.ms_stock.service;

import com.restaurante.ms_stock.dto.IngredienteRequestDTO;
import com.restaurante.ms_stock.dto.IngredienteResponseDTO;

import java.util.List;

public interface IngredienteService {
    IngredienteResponseDTO crear(IngredienteRequestDTO request);
    List<IngredienteResponseDTO> listar();
    IngredienteResponseDTO obtenerPorId(Long id);
    IngredienteResponseDTO actualizar(Long id, IngredienteRequestDTO request);
    void eliminar(Long id);
}

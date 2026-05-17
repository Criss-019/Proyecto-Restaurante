package com.restaurante.ms_catalogo.service;

import com.restaurante.ms_catalogo.dto.PlatoRequestDTO;
import com.restaurante.ms_catalogo.dto.PlatoResponseDTO;
import java.util.List;

public interface PlatoService {
    PlatoResponseDTO crearPlato(PlatoRequestDTO request);
    List<PlatoResponseDTO> obtenerTodos();
    PlatoResponseDTO obtenerPorId(Long id);
    PlatoResponseDTO actualizarPlato(Long id, PlatoRequestDTO request);
    void eliminarPlato(Long id);
}


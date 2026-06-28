package com.restaurante.ms_clientes.service;

import com.restaurante.ms_clientes.dto.ClienteRequestDTO;
import com.restaurante.ms_clientes.dto.ClienteResponseDTO;
import java.util.List;

public interface ClienteService {
    ClienteResponseDTO crearCliente(ClienteRequestDTO request);
    List<ClienteResponseDTO> obtenerTodos();
    ClienteResponseDTO obtenerPorId(Long id);
    ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO request);
    void eliminarCliente(Long id);
}

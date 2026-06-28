package com.restaurante.ms_clientes.service;

import com.restaurante.ms_clientes.dto.ClienteRequestDTO;
import com.restaurante.ms_clientes.dto.ClienteResponseDTO;
import com.restaurante.ms_clientes.entity.Cliente;
import com.restaurante.ms_clientes.exception.ResourceNotFoundException;
import com.restaurante.ms_clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    public ClienteResponseDTO crearCliente(ClienteRequestDTO request) {
        log.info("Iniciando creación de cliente con email: {}", request.getEmail());
        
        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .build();
        
        Cliente guardado = clienteRepository.save(cliente);
        log.info("Cliente creado exitosamente con ID: {}", guardado.getId());
        return mapToDTO(guardado);
    }

    @Override
    public List<ClienteResponseDTO> obtenerTodos() {
        log.info("Consultando todos los clientes");
        return clienteRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteResponseDTO obtenerPorId(Long id) {
        log.info("Consultando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToDTO(cliente);
    }

    @Override
    public ClienteResponseDTO actualizarCliente(Long id, ClienteRequestDTO request) {
        log.info("Actualizando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        
        cliente.setNombre(request.getNombre());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        
        Cliente actualizado = clienteRepository.save(cliente);
        log.info("Cliente con ID: {} actualizado exitosamente", id);
        return mapToDTO(actualizado);
    }

    @Override
    public void eliminarCliente(Long id) {
        log.info("Eliminando cliente con ID: {}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        clienteRepository.delete(cliente);
        log.info("Cliente con ID: {} eliminado exitosamente", id);
    }

    private ClienteResponseDTO mapToDTO(Cliente cliente) {
        return ClienteResponseDTO.builder()
                .id(cliente.getId())
                .nombre(cliente.getNombre())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .build();
    }
}

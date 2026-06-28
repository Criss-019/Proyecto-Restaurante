package com.restaurante.ms_clientes.repository;

import com.restaurante.ms_clientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email); // Método útil para evitar duplicados en el Service
}

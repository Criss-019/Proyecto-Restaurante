package com.restaurante.ms_reservas.repository;

import com.restaurante.ms_reservas.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {
    List<Reserva> findByClienteId(Long clienteId); // Útil para listar reservas de un cliente específico
}

package com.restaurante.ms_cocina.repository;

import com.restaurante.ms_cocina.entity.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComandaRepository extends JpaRepository<Comanda, Long> {
    // Método útil para buscar todos los platos que pertenecen a una misma orden (pedido)
    List<Comanda> findByPedidoId(Long pedidoId); 
}

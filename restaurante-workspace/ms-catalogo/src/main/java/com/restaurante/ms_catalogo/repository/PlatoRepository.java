package com.restaurante.ms_catalogo.repository;

import com.restaurante.ms_catalogo.entity.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {
    Optional<Plato> findByNombre(String nombre);
}


package com.restaurante.ms_catalogo.repository;

import com.restaurante.ms_catalogo.model.Plato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatoRepository extends JpaRepository<Plato, Long> {
    // Solo con extender JpaRepository, Spring nos regala los métodos:
    // save(), findAll(), findById(), deleteById() sin que tengamos que programarlos.
}


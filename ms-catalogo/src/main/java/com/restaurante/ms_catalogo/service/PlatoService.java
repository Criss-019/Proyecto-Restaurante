package com.restaurante.ms_catalogo.service;

import com.restaurante.ms_catalogo.model.Plato;
import com.restaurante.ms_catalogo.repository.PlatoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlatoService {

    @Autowired
    private PlatoRepository repository;

    // Método para obtener todos los platos
    public List<Plato> listarTodos() {
        return repository.findAll();
    }

    // Método para guardar un nuevo plato
    public Plato guardarPlato(Plato plato) {
        return repository.save(plato);
    }
}


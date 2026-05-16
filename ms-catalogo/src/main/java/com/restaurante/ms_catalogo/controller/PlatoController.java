package com.restaurante.ms_catalogo.controller;

import com.restaurante.ms_catalogo.model.Plato;
import com.restaurante.ms_catalogo.service.PlatoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogo") // Esta será la URL base para este microservicio
public class PlatoController {

    @Autowired
    private PlatoService service;

    // Endpoint GET: http://localhost:8081/api/catalogo/platos
    @GetMapping("/platos")
    public ResponseEntity<List<Plato>> listarPlatos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    // Endpoint POST: http://localhost:8081/api/catalogo/platos
    @PostMapping("/platos")
    public ResponseEntity<Plato> crearPlato(@RequestBody Plato plato) {
        return ResponseEntity.ok(service.guardarPlato(plato));
    }
}


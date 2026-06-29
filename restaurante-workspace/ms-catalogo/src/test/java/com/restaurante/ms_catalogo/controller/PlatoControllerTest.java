package com.restaurante.ms_catalogo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_catalogo.dto.PlatoRequestDTO;
import com.restaurante.ms_catalogo.dto.PlatoResponseDTO;
import com.restaurante.ms_catalogo.service.PlatoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

/**
 * Pruebas unitarias para PlatoController.
 *
 * Estrategia:
 *   - PlatoController es REAL (instanciado por @WebMvcTest)
 *   - PlatoService es SIMULADO con @MockBean (no toca BD ni Eureka)
 *   - MockMvc simula las peticiones HTTP (GET, POST, PUT, DELETE)
 */
@WebMvcTest(PlatoController.class)
class PlatoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // PlatoService simulado: no llama a la BD real
    @MockBean
    private PlatoService platoService;

    // ObjectMapper para convertir objetos Java a JSON
    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1: POST /api/catalogo/platos — crearPlato()
    // =========================================================

    @Test
    @DisplayName("POST /api/catalogo/platos - debería crear un plato y retornar HTTP 201")
    void crearPlato_cuandoDatosValidos_deberiaRetornar201() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar datos de entrada y respuesta mock
        // --------------------------------------------------
        PlatoRequestDTO request = new PlatoRequestDTO();
        request.setNombre("Pasta Carbonara");
        request.setDescripcion("Pasta con salsa cremosa y panceta");
        request.setPrecio(9500.0);
        request.setDisponible(true);

        PlatoResponseDTO response = PlatoResponseDTO.builder()
                .id(1L)
                .nombre("Pasta Carbonara")
                .descripcion("Pasta con salsa cremosa y panceta")
                .precio(9500.0)
                .disponible(true)
                .build();

        // Cuando el service reciba cualquier PlatoRequestDTO, devuelve la respuesta simulada
        when(platoService.crearPlato(any(PlatoRequestDTO.class))).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición POST con MockMvc
        // --------------------------------------------------
        mockMvc.perform(post("/api/catalogo/platos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar estado HTTP y campos del JSON devuelto
                // --------------------------------------------------
                .andExpect(status().isCreated())                         // HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pasta Carbonara"))
                .andExpect(jsonPath("$.precio").value(9500.0))
                .andExpect(jsonPath("$.disponible").value(true));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(platoService).crearPlato(any(PlatoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller retornara HttpStatus.OK (200) en lugar de HttpStatus.CREATED (201),
         * el test fallaría con:
         *
         *   Expected: 201
         *   But was:  200
         *
         * QA debería reportar:
         *   - Endpoint: POST /api/catalogo/platos
         *   - Se esperaba: HTTP 201 Created
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok() en lugar de
         *     new ResponseEntity<>(response, HttpStatus.CREATED)
         */
    }

    // =========================================================
    // TEST 2: GET /api/catalogo/platos — obtenerTodos()
    // =========================================================

    @Test
    @DisplayName("GET /api/catalogo/platos - debería retornar lista de platos y HTTP 200")
    void obtenerTodos_cuandoExistenPlatos_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar lista simulada con 2 platos
        // --------------------------------------------------
        PlatoResponseDTO plato1 = PlatoResponseDTO.builder()
                .id(1L)
                .nombre("Pasta Carbonara")
                .descripcion("Pasta con salsa cremosa y panceta")
                .precio(9500.0)
                .disponible(true)
                .build();

        PlatoResponseDTO plato2 = PlatoResponseDTO.builder()
                .id(2L)
                .nombre("Pizza Margherita")
                .descripcion("Pizza clásica con tomate y mozzarella")
                .precio(8900.0)
                .disponible(false)
                .build();

        List<PlatoResponseDTO> listaPlatos = Arrays.asList(plato1, plato2);

        // Cuando el service sea llamado, devuelve la lista simulada
        when(platoService.obtenerTodos()).thenReturn(listaPlatos);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET sin parámetros
        // --------------------------------------------------
        mockMvc.perform(get("/api/catalogo/platos")
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))              // La lista tiene 2 elementos
                .andExpect(jsonPath("$[0].id").value(1))                 // Primer plato: id=1
                .andExpect(jsonPath("$[0].nombre").value("Pasta Carbonara"))
                .andExpect(jsonPath("$[0].precio").value(9500.0))
                .andExpect(jsonPath("$[1].id").value(2))                 // Segundo plato: id=2
                .andExpect(jsonPath("$[1].nombre").value("Pizza Margherita"))
                .andExpect(jsonPath("$[1].disponible").value(false));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(platoService, times(1)).obtenerTodos();

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service devolviera una lista vacía cuando debería haber platos,
         * el test fallaría con:
         *
         *   Expected: $.length() == 2
         *   But was:  0
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/catalogo/platos
         *   - Se esperaba: JSON array con 2 platos
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: el repositorio no está guardando los datos correctamente
         *     o el mock del service no fue configurado antes de ejecutar el test
         */
    }

    // =========================================================
    // TEST 3: GET /api/catalogo/platos/{id} — obtenerPorId()
    // =========================================================

    @Test
    @DisplayName("GET /api/catalogo/platos/{id} - debería retornar el plato encontrado y HTTP 200")
    void obtenerPorId_cuandoIdExiste_deberiaRetornar200ConPlato() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el plato simulado que devolverá el service
        // --------------------------------------------------
        Long idBuscado = 1L;

        PlatoResponseDTO platoEncontrado = PlatoResponseDTO.builder()
                .id(idBuscado)
                .nombre("Pasta Carbonara")
                .descripcion("Pasta con salsa cremosa y panceta")
                .precio(9500.0)
                .disponible(true)
                .build();

        // Cuando el service reciba el id=1, devuelve el plato simulado
        when(platoService.obtenerPorId(idBuscado)).thenReturn(platoEncontrado);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/catalogo/platos/{id}", idBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos del plato devuelto
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pasta Carbonara"))
                .andExpect(jsonPath("$.descripcion").value("Pasta con salsa cremosa y panceta"))
                .andExpect(jsonPath("$.precio").value(9500.0))
                .andExpect(jsonPath("$.disponible").value(true));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado con el ID correcto
        // --------------------------------------------------
        verify(platoService, times(1)).obtenerPorId(idBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service lanzara una excepción (ej: plato no encontrado)
         * y el controller no la manejara correctamente, el test fallaría con:
         *
         *   Expected: 200
         *   But was:  500 (Internal Server Error)
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/catalogo/platos/1
         *   - Se esperaba: HTTP 200 con el plato en el cuerpo
         *   - Se obtuvo:   HTTP 500 sin cuerpo útil
         *   - Causa probable: falta un @ExceptionHandler o @ControllerAdvice
         *     que traduzca la excepción en HTTP 404 Not Found
         */
    }

    // =========================================================
    // TEST 4: PUT /api/catalogo/platos/{id} — actualizarPlato()
    // =========================================================

    @Test
    @DisplayName("PUT /api/catalogo/platos/{id} - debería actualizar el plato y retornar HTTP 200")
    void actualizarPlato_cuandoDatosValidos_deberiaRetornar200ConPlatoActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, el body de actualización y la respuesta simulada
        // --------------------------------------------------
        Long idActualizar = 1L;

        // Datos nuevos que el cliente envía para actualizar el plato
        PlatoRequestDTO requestActualizado = new PlatoRequestDTO();
        requestActualizado.setNombre("Pasta Carbonara Premium");
        requestActualizado.setDescripcion("Pasta con salsa cremosa, panceta y trufa negra");
        requestActualizado.setPrecio(12500.0);
        requestActualizado.setDisponible(true);

        // Plato con los datos ya actualizados que devuelve el service
        PlatoResponseDTO responseActualizado = PlatoResponseDTO.builder()
                .id(idActualizar)
                .nombre("Pasta Carbonara Premium")
                .descripcion("Pasta con salsa cremosa, panceta y trufa negra")
                .precio(12500.0)
                .disponible(true)
                .build();

        // Cuando el service reciba el id=1 y CUALQUIER PlatoRequestDTO (any), devuelve el plato actualizado.
        // NOTA: usamos any() porque Jackson deserializa el JSON en un objeto NUEVO en memoria,
        // diferente a 'requestActualizado'. Sin @EqualsAndHashCode en el DTO, Mockito
        // no puede comparar por valor y el mock devolvería null.
        when(platoService.actualizarPlato(any(Long.class), any(PlatoRequestDTO.class)))
                .thenReturn(responseActualizado);

        // --------------------------------------------------
        // ACT: ejecutar la petición PUT con ID en URL y body JSON
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/catalogo/platos/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestActualizado)))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos actualizados
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Pasta Carbonara Premium"))
                .andExpect(jsonPath("$.descripcion").value("Pasta con salsa cremosa, panceta y trufa negra"))
                .andExpect(jsonPath("$.precio").value(12500.0))
                .andExpect(jsonPath("$.disponible").value(true));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado con los parámetros correctos
        // --------------------------------------------------
        verify(platoService, times(1)).actualizarPlato(any(Long.class), any(PlatoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el precio no se actualizara correctamente y el service devolviera
         * el precio anterior (9500.0) en vez del nuevo (12500.0), el test fallaría con:
         *
         *   Expected: $.precio == 12500.0
         *   But was:  9500.0
         *
         * QA debería reportar:
         *   - Endpoint: PUT /api/catalogo/platos/1
         *   - Se esperaba: JSON con precio = 12500.0
         *   - Se obtuvo:   JSON con precio = 9500.0
         *   - Causa probable: el método actualizarPlato() en el service
         *     no está mapeando correctamente el campo precio del request al entity
         */
    }

    // =========================================================
    // TEST 5: DELETE /api/catalogo/platos/{id} — eliminarPlato()
    // =========================================================

    @Test
    @DisplayName("DELETE /api/catalogo/platos/{id} - debería eliminar el plato y retornar HTTP 204")
    void eliminarPlato_cuandoIdExiste_deberiaRetornar204SinContenido() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID del plato a eliminar
        // --------------------------------------------------
        Long idEliminar = 1L;

        // eliminarPlato() es void → usamos doNothing() para simular que no lanza excepciones
        // (comportamiento normal: el plato existe y se elimina correctamente)
        org.mockito.Mockito.doNothing().when(platoService).eliminarPlato(idEliminar);

        // --------------------------------------------------
        // ACT: ejecutar la petición DELETE con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/catalogo/platos/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 204 y que el body esté vacío
                // --------------------------------------------------
                .andExpect(status().isNoContent())       // HTTP 204 No Content
                .andExpect(content().string(""));        // Body completamente vacío

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(platoService, times(1)).eliminarPlato(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller devolviera HTTP 200 OK en lugar de HTTP 204 No Content,
         * el test fallaría con:
         *
         *   Expected: 204
         *   But was:  200
         *
         * QA debería reportar:
         *   - Endpoint: DELETE /api/catalogo/platos/1
         *   - Se esperaba: HTTP 204 No Content (body vacío)
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok().build()
         *     en lugar de ResponseEntity.noContent().build()
         *
         * Segundo escenario de falla posible:
         *   Si el service lanzara una excepción porque el ID no existe,
         *   y no hay @ControllerAdvice que la maneje, el test fallaría con:
         *
         *   Expected: 204
         *   But was:  500
         *
         *   En ese caso desarrollo debe agregar manejo de excepciones con
         *   @ExceptionHandler para devolver HTTP 404 Not Found.
         */
    }
}

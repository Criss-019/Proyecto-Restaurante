package com.restaurante.ms_stock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_stock.dto.IngredienteRequestDTO;
import com.restaurante.ms_stock.dto.IngredienteResponseDTO;
import com.restaurante.ms_stock.service.IngredienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para IngredienteController.
 *
 * Estrategia:
 * - Se usa @WebMvcTest para cargar solo la capa web (sin base de datos ni contexto completo).
 * - IngredienteService se simula con @MockBean para aislar el controlador.
 * - Se usa MockMvc para simular peticiones HTTP reales.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 * - Base URL: /api/stock/ingredientes
 */
@WebMvcTest(IngredienteController.class)
class IngredienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IngredienteService service;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    // =========================================================================
    // POST /api/stock/ingredientes — crear()
    // =========================================================================

    @Test
    @DisplayName("POST /api/stock/ingredientes - Debe crear un ingrediente y retornar HTTP 201 Created")
    void crear_DebeRetornar201Created_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar datos de entrada y respuesta simulada del servicio
        IngredienteRequestDTO request = new IngredienteRequestDTO();
        request.setNombre("Harina");
        request.setCantidadActual(50.0);
        request.setCantidadMinima(10.0);
        request.setUnidadMedida("Kilogramos");

        IngredienteResponseDTO responseSimulado = IngredienteResponseDTO.builder()
                .id(1L)
                .nombre("Harina")
                .cantidadActual(50.0)
                .cantidadMinima(10.0)
                .unidadMedida("Kilogramos")
                .build();

        when(service.crear(any(IngredienteRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP POST enviando el JSON en el cuerpo
        mockMvc.perform(post("/api/stock/ingredientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar HTTP 201 Created y los atributos del JSON devuelto
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Harina"))
                .andExpect(jsonPath("$.cantidadActual").value(50.0))
                .andExpect(jsonPath("$.cantidadMinima").value(10.0))
                .andExpect(jsonPath("$.unidadMedida").value("Kilogramos"));

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez
        verify(service, times(1)).crear(any(IngredienteRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 201 Created se obtiene HTTP 400 Bad Request:
         * - QA debe reportar: "Falla al crear ingrediente: La API responde 400 Bad Request al enviar IngredienteRequestDTO válido."
         * - Desarrollo debe revisar: Las anotaciones @NotBlank, @NotNull y @Min en los campos de IngredienteRequestDTO.
         */
    }

    // =========================================================================
    // GET /api/stock/ingredientes — listar()
    // =========================================================================

    @Test
    @DisplayName("GET /api/stock/ingredientes - Debe retornar lista de ingredientes con HTTP 200 OK")
    void listar_DebeRetornar200OK_YListaDeIngredientes() throws Exception {
        // ARRANGE: Preparar lista simulada de ingredientes
        IngredienteResponseDTO ingrediente1 = IngredienteResponseDTO.builder()
                .id(1L)
                .nombre("Harina")
                .cantidadActual(50.0)
                .cantidadMinima(10.0)
                .unidadMedida("Kilogramos")
                .build();

        IngredienteResponseDTO ingrediente2 = IngredienteResponseDTO.builder()
                .id(2L)
                .nombre("Aceite de oliva")
                .cantidadActual(8.5)
                .cantidadMinima(2.0)
                .unidadMedida("Litros")
                .build();

        when(service.listar()).thenReturn(java.util.Arrays.asList(ingrediente1, ingrediente2));

        // ACT: Ejecutar la petición HTTP GET al endpoint de listado
        mockMvc.perform(get("/api/stock/ingredientes")
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK, tamaño del arreglo y atributos clave
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Harina"))
                .andExpect(jsonPath("$[0].unidadMedida").value("Kilogramos"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Aceite de oliva"))
                .andExpect(jsonPath("$[1].cantidadActual").value(8.5));

        // VERIFY: Comprobar invocación única al servicio
        verify(service, times(1)).listar();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla al listar ingredientes: El servidor respondí 500 en GET /api/stock/ingredientes."
         * - Desarrollo debe revisar: Excepciones no controladas en IngredienteService.listar() o errores de mapeo a DTO.
         */
    }

    // =========================================================================
    // GET /api/stock/ingredientes/{id} — obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("GET /api/stock/ingredientes/{id} - Debe retornar un ingrediente existente con HTTP 200 OK")
    void obtenerPorId_DebeRetornar200OK_CuandoIngredienteExiste() throws Exception {
        // ARRANGE: Preparar datos simulados para un ingrediente con ID 1L
        Long idBusqueda = 1L;

        IngredienteResponseDTO responseSimulado = IngredienteResponseDTO.builder()
                .id(idBusqueda)
                .nombre("Harina")
                .cantidadActual(50.0)
                .cantidadMinima(10.0)
                .unidadMedida("Kilogramos")
                .build();

        when(service.obtenerPorId(idBusqueda)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP GET con el ID en la URL
        mockMvc.perform(get("/api/stock/ingredientes/{id}", idBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y coincidencia de atributos en el JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Harina"))
                .andExpect(jsonPath("$.cantidadActual").value(50.0))
                .andExpect(jsonPath("$.cantidadMinima").value(10.0))
                .andExpect(jsonPath("$.unidadMedida").value("Kilogramos"));

        // VERIFY: Comprobar que el servicio fue llamado con el ID correcto
        verify(service, times(1)).obtenerPorId(idBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 404 Not Found cuando el ingrediente existe:
         * - QA debe reportar: "Falla al obtener ingrediente por ID: La API devuelve 404 Not Found para un ID válido."
         * - Desarrollo debe revisar: La anotación @PathVariable Long id o la lógica en IngredienteService.obtenerPorId().
         */
    }

    // =========================================================================
    // PUT /api/stock/ingredientes/{id} — actualizar()
    // =========================================================================

    @Test
    @DisplayName("PUT /api/stock/ingredientes/{id} - Debe actualizar un ingrediente y retornar HTTP 200 OK")
    void actualizar_DebeRetornar200OK_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar ID, solicitud con datos actualizados y respuesta simulada
        Long idActualizar = 1L;

        IngredienteRequestDTO request = new IngredienteRequestDTO();
        request.setNombre("Harina Integral");
        request.setCantidadActual(35.5);
        request.setCantidadMinima(15.0);
        request.setUnidadMedida("Kilogramos");

        IngredienteResponseDTO responseSimulado = IngredienteResponseDTO.builder()
                .id(idActualizar)
                .nombre("Harina Integral")
                .cantidadActual(35.5)
                .cantidadMinima(15.0)
                .unidadMedida("Kilogramos")
                .build();

        when(service.actualizar(eq(idActualizar), any(IngredienteRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PUT enviando el ID en la URL y el JSON en el cuerpo
        mockMvc.perform(put("/api/stock/ingredientes/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia de datos actualizados
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Harina Integral"))
                .andExpect(jsonPath("$.cantidadActual").value(35.5))
                .andExpect(jsonPath("$.cantidadMinima").value(15.0))
                .andExpect(jsonPath("$.unidadMedida").value("Kilogramos"));

        // VERIFY: Comprobar invocación única del servicio con los parámetros correctos
        verify(service, times(1)).actualizar(eq(idActualizar), any(IngredienteRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por validación:
         * - QA debe reportar: "Falla al actualizar ingrediente: La API devuelve 400 Bad Request al enviar datos válidos."
         * - Desarrollo debe revisar: Las anotaciones de validación (e.g. @Min, @Size, @NotBlank) en IngredienteRequestDTO.
         */
    }

    // =========================================================================
    // DELETE /api/stock/ingredientes/{id} — eliminar()
    // =========================================================================

    @Test
    @DisplayName("DELETE /api/stock/ingredientes/{id} - Debe eliminar un ingrediente y retornar HTTP 204 No Content")
    void eliminar_DebeRetornar204NoContent_CuandoIngredienteExiste() throws Exception {
        // ARRANGE: Preparar ID del ingrediente a eliminar
        Long idEliminar = 1L;

        // Simular que el servicio ejecuta la eliminación sin retornar valor (void)
        doNothing().when(service).eliminar(idEliminar);

        // ACT: Ejecutar la petición HTTP DELETE con el ID en la URL
        mockMvc.perform(delete("/api/stock/ingredientes/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 204 No Content (sin cuerpo en la respuesta)
                .andExpect(status().isNoContent());

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez con el ID correcto
        verify(service, times(1)).eliminar(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 204 No Content se obtiene HTTP 404 Not Found:
         * - QA debe reportar: "Falla al eliminar ingrediente: La API responde 404 Not Found para un ID existente."
         * - Desarrollo debe revisar: La lógica en IngredienteService.eliminar() y si se está
         *   lanzando una excepción ResourceNotFoundException al no encontrar el ingrediente.
         */
    }
}


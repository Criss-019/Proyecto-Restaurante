package com.restaurante.ms_cocina.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_cocina.dto.ComandaRequestDTO;
import com.restaurante.ms_cocina.dto.ComandaResponseDTO;
import com.restaurante.ms_cocina.service.ComandaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para ComandaController.
 *
 * Estrategia:
 *   - ComandaController es REAL (instanciado por @WebMvcTest)
 *   - ComandaService es SIMULADO con @MockBean (no toca BD ni Eureka)
 *   - MockMvc simula las peticiones HTTP (GET, POST, PUT, PATCH, DELETE)
 */
@WebMvcTest(ComandaController.class)
class ComandaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComandaService comandaService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1: POST /api/cocina/comandas — crear()
    // =========================================================

    @Test
    @DisplayName("POST /api/cocina/comandas - debería crear una comanda y retornar HTTP 201")
    void crear_cuandoDatosValidos_deberiaRetornar201() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar datos de entrada y respuesta mock
        // --------------------------------------------------
        ComandaRequestDTO request = new ComandaRequestDTO();
        request.setPedidoId(10L);
        request.setPlatoId(5L);
        request.setCantidad(2);
        request.setNotas("Sin cebolla por favor");

        ComandaResponseDTO response = ComandaResponseDTO.builder()
                .id(1L)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla por favor")
                .build();

        // Cuando el service reciba cualquier ComandaRequestDTO, devuelve la respuesta simulada
        when(comandaService.crearComanda(any(ComandaRequestDTO.class))).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición POST con MockMvc
        // --------------------------------------------------
        mockMvc.perform(post("/api/cocina/comandas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar estado HTTP y campos del JSON devuelto
                // --------------------------------------------------
                .andExpect(status().isCreated())                         // HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(10))
                .andExpect(jsonPath("$.platoId").value(5))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.notas").value("Sin cebolla por favor"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(comandaService, times(1)).crearComanda(any(ComandaRequestDTO.class));

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
         *   - Endpoint: POST /api/cocina/comandas
         *   - Se esperaba: HTTP 201 Created
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok() en lugar de
         *     new ResponseEntity<>(response, HttpStatus.CREATED)
         */
    }

    // =========================================================
    // TEST 2: GET /api/cocina/comandas — listar()
    // =========================================================

    @Test
    @DisplayName("GET /api/cocina/comandas - debería retornar lista de comandas y HTTP 200")
    void listar_cuandoExistenComandas_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar lista simulada con 2 comandas
        // --------------------------------------------------
        ComandaResponseDTO comanda1 = ComandaResponseDTO.builder()
                .id(1L)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        ComandaResponseDTO comanda2 = ComandaResponseDTO.builder()
                .id(2L)
                .pedidoId(10L)
                .platoId(3L)
                .cantidad(1)
                .estado("PREPARANDO")
                .notas("Bien cocido")
                .build();

        List<ComandaResponseDTO> lista = Arrays.asList(comanda1, comanda2);

        // Cuando el service sea llamado, devuelve la lista simulada
        when(comandaService.obtenerTodas()).thenReturn(lista);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET
        // --------------------------------------------------
        mockMvc.perform(get("/api/cocina/comandas")
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].estado").value("PREPARANDO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(comandaService, times(1)).obtenerTodas();

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service devolviera una lista vacía cuando debería haber comandas,
         * el test fallaría con:
         *
         *   Expected: $.length() == 2
         *   But was:  0
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/cocina/comandas
         *   - Se esperaba: JSON array con 2 comandas
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: el mock del service no fue configurado antes de ejecutar el test,
         *     o la lógica del controller no está retornando la respuesta del service.
         */
    }

    // =========================================================
    // TEST 3: GET /api/cocina/comandas/{id} — obtenerPorId()
    // =========================================================

    @Test
    @DisplayName("GET /api/cocina/comandas/{id} - debería retornar la comanda encontrada y HTTP 200")
    void obtenerPorId_cuandoIdExiste_deberiaRetornar200ConComanda() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el id y la comanda simulada
        // --------------------------------------------------
        Long idBuscado = 1L;

        ComandaResponseDTO comandaEncontrada = ComandaResponseDTO.builder()
                .id(idBuscado)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        // Cuando el service reciba el id=1, devuelve la comanda simulada
        when(comandaService.obtenerPorId(idBuscado)).thenReturn(comandaEncontrada);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con ID en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/cocina/comandas/{id}", idBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos de la comanda devuelta
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(10))
                .andExpect(jsonPath("$.platoId").value(5))
                .andExpect(jsonPath("$.cantidad").value(2))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.notas").value("Sin cebolla"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(comandaService, times(1)).obtenerPorId(idBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service lanzara una excepción por no encontrar la comanda,
         * y no estuviera manejada, el test fallaría con:
         *
         *   Expected: 200
         *   But was:  500
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/cocina/comandas/1
         *   - Se esperaba: HTTP 200 OK con la comanda
         *   - Se obtuvo:   HTTP 500 Internal Server Error
         *   - Causa probable: Falta implementar un handler de excepciones que
         *     capture el EntityNotFoundException y retorne HTTP 404.
         */
    }

    // =========================================================
    // TEST 4: GET /api/cocina/comandas/pedido/{pedidoId} — obtenerPorPedidoId()
    // =========================================================

    @Test
    @DisplayName("GET /api/cocina/comandas/pedido/{pedidoId} - debería retornar las comandas del pedido y HTTP 200")
    void obtenerPorPedidoId_cuandoPedidoExiste_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el pedidoId y una lista de comandas simuladas
        // --------------------------------------------------
        Long pedidoIdBuscado = 10L;

        ComandaResponseDTO comanda1 = ComandaResponseDTO.builder()
                .id(1L)
                .pedidoId(pedidoIdBuscado)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        ComandaResponseDTO comanda2 = ComandaResponseDTO.builder()
                .id(2L)
                .pedidoId(pedidoIdBuscado)
                .platoId(3L)
                .cantidad(1)
                .estado("PREPARANDO")
                .notas("Bien cocido")
                .build();

        List<ComandaResponseDTO> listaComandas = Arrays.asList(comanda1, comanda2);

        // Cuando el service reciba el pedidoId=10, devuelve la lista de comandas simuladas
        when(comandaService.obtenerPorPedidoId(pedidoIdBuscado)).thenReturn(listaComandas);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el pedidoId en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/cocina/comandas/pedido/{pedidoId}", pedidoIdBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pedidoId").value(10))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].pedidoId").value(10))
                .andExpect(jsonPath("$[1].id").value(2));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el pedidoId correcto
        // --------------------------------------------------
        verify(comandaService, times(1)).obtenerPorPedidoId(pedidoIdBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller tuviera un error en la ruta mapping o no pasara
         * correctamente el pedidoId, el test fallaría. Si devolviera una
         * lista vacía en lugar de 2 registros, el test fallaría con:
         *
         *   Expected: $.length() == 2
         *   But was:  0
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/cocina/comandas/pedido/10
         *   - Se esperaba: JSON array con 2 comandas
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: El método del controller no está pasando
         *     el pedidoId correcto al service o la llamada falla silenciosamente.
         */
    }

    // =========================================================
    // TEST 5: PUT /api/cocina/comandas/{id} — actualizar()
    // =========================================================

    @Test
    @DisplayName("PUT /api/cocina/comandas/{id} - debería actualizar la comanda y retornar HTTP 200")
    void actualizar_cuandoDatosValidos_deberiaRetornar200ConComandaActualizada() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, el request body y el response simulado
        // --------------------------------------------------
        Long idActualizar = 1L;

        ComandaRequestDTO request = new ComandaRequestDTO();
        request.setPedidoId(10L);
        request.setPlatoId(5L);
        request.setCantidad(3); // Aumentó la cantidad a preparar
        request.setNotas("Sin cebolla y extra picante");

        ComandaResponseDTO response = ComandaResponseDTO.builder()
                .id(idActualizar)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(3)
                .estado("PENDIENTE")
                .notas("Sin cebolla y extra picante")
                .build();

        // Configurar mock con any() para evitar problemas por falta de equals/hashCode en el request DTO
        when(comandaService.actualizarComanda(any(Long.class), any(ComandaRequestDTO.class)))
                .thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PUT con el ID en la URL y el JSON en el body
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/cocina/comandas/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y campos actualizados
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidad").value(3))
                .andExpect(jsonPath("$.notas").value("Sin cebolla y extra picante"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con los tipos correctos
        // --------------------------------------------------
        verify(comandaService, times(1)).actualizarComanda(any(Long.class), any(ComandaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si la cantidad no se actualizara y el service retornara la cantidad vieja (2),
         * el test fallaría con:
         *
         *   Expected: $.cantidad == 3
         *   But was:  2
         *
         * QA debería reportar:
         *   - Endpoint: PUT /api/cocina/comandas/1
         *   - Se esperaba: JSON con cantidad = 3
         *   - Se obtuvo:   JSON con cantidad = 2
         *   - Causa probable: El método actualizarComanda() en el service no está
         *     guardando la nueva cantidad modificada.
         */
    }

    // =========================================================
    // TEST 6: PATCH /api/cocina/comandas/{id}/estado — cambiarEstado()
    // =========================================================

    @Test
    @DisplayName("PATCH /api/cocina/comandas/{id}/estado - debería cambiar el estado y retornar HTTP 200")
    void cambiarEstado_cuandoDatosValidos_deberiaRetornar200ConEstadoActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, el nuevo estado y la respuesta del mock
        // --------------------------------------------------
        Long idComanda = 1L;
        String nuevoEstado = "PREPARANDO";

        ComandaResponseDTO response = ComandaResponseDTO.builder()
                .id(idComanda)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado(nuevoEstado)
                .notas("Sin cebolla")
                .build();

        // Configurar mock
        when(comandaService.cambiarEstado(idComanda, nuevoEstado)).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PATCH con @RequestParam
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/cocina/comandas/{id}/estado", idComanda)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y que el estado cambió
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PREPARANDO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con los parámetros correspondientes
        // --------------------------------------------------
        verify(comandaService, times(1)).cambiarEstado(idComanda, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller tuviese un error y no pasara el parámetro "estado" al service,
         * o si el service arrojara error por parámetro nulo/inválido, el test fallaría con:
         *
         *   Expected: 200
         *   But was:  400 Bad Request (o 500)
         *
         * QA debería reportar:
         *   - Endpoint: PATCH /api/cocina/comandas/1/estado?estado=PREPARANDO
         *   - Se esperaba: HTTP 200 OK con el nuevo estado
         *   - Se obtuvo:   HTTP 400 Bad Request
         *   - Causa probable: El parámetro de consulta "estado" no fue enviado o no hace match
         *     con el nombre del parámetro esperado por la firma del controller.
         */
    }

    // =========================================================
    // TEST 7: DELETE /api/cocina/comandas/{id} — eliminar()
    // =========================================================

    @Test
    @DisplayName("DELETE /api/cocina/comandas/{id} - debería eliminar la comanda y retornar HTTP 204")
    void eliminar_cuandoIdExiste_deberiaRetornar204SinContenido() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID de la comanda a eliminar
        // --------------------------------------------------
        Long idEliminar = 1L;

        // eliminarComanda() es void → usamos doNothing() para simular éxito
        org.mockito.Mockito.doNothing().when(comandaService).eliminarComanda(idEliminar);

        // --------------------------------------------------
        // ACT: ejecutar la petición DELETE con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/cocina/comandas/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 204 y que el body esté vacío
                // --------------------------------------------------
                .andExpect(status().isNoContent())       // HTTP 204 No Content
                .andExpect(content().string(""));        // Body vacío

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(comandaService, times(1)).eliminarComanda(idEliminar);

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
         *   - Endpoint: DELETE /api/cocina/comandas/1
         *   - Se esperaba: HTTP 204 No Content (body vacío)
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: El controller usó ResponseEntity.ok().build() en lugar de
         *     ResponseEntity.noContent().build().
         */
    }
}







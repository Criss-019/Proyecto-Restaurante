package com.restaurante.ms_despacho.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_despacho.dto.DespachoRequestDTO;
import com.restaurante.ms_despacho.dto.DespachoResponseDTO;
import com.restaurante.ms_despacho.service.DespachoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

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
 * Pruebas unitarias para DespachoController.
 *
 * Estrategia:
 *   - DespachoController es REAL (instanciado por @WebMvcTest)
 *   - DespachoService es SIMULADO con @MockBean (no toca BD ni Eureka)
 *   - MockMvc simula las peticiones HTTP (GET, POST, PUT, PATCH, DELETE)
 */
@WebMvcTest(DespachoController.class)
class DespachoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DespachoService despachoService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1: POST /api/despachos — programar()
    // =========================================================

    @Test
    @DisplayName("POST /api/despachos - debería programar un despacho y retornar HTTP 201")
    void programar_cuandoDatosValidos_deberiaRetornar201() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar datos de entrada y respuesta mock
        // --------------------------------------------------
        LocalDateTime fechaEstimada = LocalDateTime.of(2026, 6, 29, 21, 30);

        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(101L);
        request.setRepartidorAsignado("Carlos Gomez");
        request.setDireccionEntrega("Av. Providencia 1234, Santiago");
        request.setFechaEntregaEstimada(fechaEstimada);
        request.setEstado("PROGRAMADO");

        DespachoResponseDTO response = DespachoResponseDTO.builder()
                .id(1L)
                .pedidoId(101L)
                .repartidorAsignado("Carlos Gomez")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada)
                .estado("PROGRAMADO")
                .build();

        // Cuando el service reciba cualquier DespachoRequestDTO, devuelve la respuesta simulada
        when(despachoService.programarDespacho(any(DespachoRequestDTO.class))).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición POST con MockMvc
        // --------------------------------------------------
        mockMvc.perform(post("/api/despachos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar estado HTTP y campos del JSON devuelto
                // --------------------------------------------------
                .andExpect(status().isCreated())                         // HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.repartidorAsignado").value("Carlos Gomez"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(despachoService, times(1)).programarDespacho(any(DespachoRequestDTO.class));

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
         *   - Endpoint: POST /api/despachos
         *   - Se esperaba: HTTP 201 Created
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok() en lugar de
         *     new ResponseEntity<>(response, HttpStatus.CREATED)
         */
    }

    // =========================================================
    // TEST 2: GET /api/despachos — listar()
    // =========================================================

    @Test
    @DisplayName("GET /api/despachos - debería retornar lista de despachos y HTTP 200")
    void listar_cuandoExistenDespachos_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar lista simulada con 2 despachos
        // --------------------------------------------------
        LocalDateTime fechaEstimada1 = LocalDateTime.of(2026, 6, 29, 21, 30);
        LocalDateTime fechaEstimada2 = LocalDateTime.of(2026, 6, 29, 22, 00);

        DespachoResponseDTO despacho1 = DespachoResponseDTO.builder()
                .id(1L)
                .pedidoId(101L)
                .repartidorAsignado("Carlos Gomez")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada1)
                .estado("PROGRAMADO")
                .build();

        DespachoResponseDTO despacho2 = DespachoResponseDTO.builder()
                .id(2L)
                .pedidoId(102L)
                .repartidorAsignado("Ana Silva")
                .direccionEntrega("Av. Vitacura 5678, Santiago")
                .fechaEntregaEstimada(fechaEstimada2)
                .estado("EN_TRANSITO")
                .build();

        List<DespachoResponseDTO> lista = Arrays.asList(despacho1, despacho2);

        // Cuando el service sea llamado, devuelve la lista simulada
        when(despachoService.obtenerTodos()).thenReturn(lista);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET sin parámetros
        // --------------------------------------------------
        mockMvc.perform(get("/api/despachos")
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].repartidorAsignado").value("Carlos Gomez"))
                .andExpect(jsonPath("$[0].estado").value("PROGRAMADO"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].repartidorAsignado").value("Ana Silva"))
                .andExpect(jsonPath("$[1].estado").value("EN_TRANSITO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(despachoService, times(1)).obtenerTodos();

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service devolviera una lista vacía cuando debería haber registros,
         * el test fallaría con:
         *
         *   Expected: $.length() == 2
         *   But was:  0
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/despachos
         *   - Se esperaba: JSON array con 2 despachos
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: el repositorio no está retornando los datos correctos
         *     o el mock del service no fue configurado antes de ejecutar el test.
         */
    }

    // =========================================================
    // TEST 3: GET /api/despachos/{id} — obtenerPorId()
    // =========================================================

    @Test
    @DisplayName("GET /api/despachos/{id} - debería retornar el despacho encontrado y HTTP 200")
    void obtenerPorId_cuandoIdExiste_deberiaRetornar200ConDespacho() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el id y el despacho simulado
        // --------------------------------------------------
        Long idBuscado = 1L;
        LocalDateTime fechaEstimada = LocalDateTime.of(2026, 6, 29, 21, 30);

        DespachoResponseDTO despachoEncontrado = DespachoResponseDTO.builder()
                .id(idBuscado)
                .pedidoId(101L)
                .repartidorAsignado("Carlos Gomez")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada)
                .estado("PROGRAMADO")
                .build();

        // Cuando el service reciba el id=1, devuelve el despacho simulado
        when(despachoService.obtenerPorId(idBuscado)).thenReturn(despachoEncontrado);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/despachos/{id}", idBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos del despacho devuelto
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.repartidorAsignado").value("Carlos Gomez"))
                .andExpect(jsonPath("$.direccionEntrega").value("Av. Providencia 1234, Santiago"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(despachoService, times(1)).obtenerPorId(idBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service lanzara una excepción por no encontrar el despacho,
         * y no estuviera manejada, el test fallaría con:
         *
         *   Expected: 200
         *   But was:  500
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/despachos/1
         *   - Se esperaba: HTTP 200 OK con el despacho
         *   - Se obtuvo:   HTTP 500 Internal Server Error
         *   - Causa probable: Falta implementar un handler de excepciones que
         *     capture el EntityNotFoundException y retorne HTTP 404.
         */
    }

    // =========================================================
    // TEST 4: GET /api/despachos/pedido/{pedidoId} — obtenerPorPedidoId()
    // =========================================================

    @Test
    @DisplayName("GET /api/despachos/pedido/{pedidoId} - debería retornar el despacho del pedido y HTTP 200")
    void obtenerPorPedidoId_cuandoPedidoExiste_deberiaRetornar200ConDespacho() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el pedidoId y el despacho simulado
        // --------------------------------------------------
        Long pedidoIdBuscado = 101L;
        LocalDateTime fechaEstimada = LocalDateTime.of(2026, 6, 29, 21, 30);

        DespachoResponseDTO despachoEncontrado = DespachoResponseDTO.builder()
                .id(1L)
                .pedidoId(pedidoIdBuscado)
                .repartidorAsignado("Carlos Gomez")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada)
                .estado("PROGRAMADO")
                .build();

        // Cuando el service reciba el pedidoId=101, devuelve el despacho simulado
        when(despachoService.obtenerPorPedidoId(pedidoIdBuscado)).thenReturn(despachoEncontrado);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el pedidoId en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/despachos/pedido/{pedidoId}", pedidoIdBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y campos del despacho devuelto
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.repartidorAsignado").value("Carlos Gomez"))
                .andExpect(jsonPath("$.estado").value("PROGRAMADO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el pedidoId correcto
        // --------------------------------------------------
        verify(despachoService, times(1)).obtenerPorPedidoId(pedidoIdBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller tuviera una ruta mapeada incorrectamente o no pasara
         * el pedidoId al service, el test fallaría. Si no encontrara el despacho
         * y retornara una respuesta vacía o error, fallaría con:
         *
         *   Expected: 200
         *   But was:  404 (o 500)
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/despachos/pedido/101
         *   - Se esperaba: HTTP 200 OK con el cuerpo del despacho
         *   - Se obtuvo:   HTTP 404 Not Found (o 500)
         *   - Causa probable: El mapping del endpoint no es correcto, o la llamada
         *     al service no está configurada de forma adecuada en el controller.
         */
    }

    // =========================================================
    // TEST 5: PUT /api/despachos/{id} — actualizar()
    // =========================================================

    @Test
    @DisplayName("PUT /api/despachos/{id} - debería actualizar el despacho y retornar HTTP 200")
    void actualizar_cuandoDatosValidos_deberiaRetornar200ConDespachoActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, request body y response simulado
        // --------------------------------------------------
        Long idActualizar = 1L;
        LocalDateTime fechaEstimada = LocalDateTime.of(2026, 6, 29, 21, 30);

        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(101L);
        request.setRepartidorAsignado("Carlos Gomez Modificado");
        request.setDireccionEntrega("Av. Providencia 1234, Santiago");
        request.setFechaEntregaEstimada(fechaEstimada);
        request.setEstado("EN_TRANSITO");

        DespachoResponseDTO response = DespachoResponseDTO.builder()
                .id(idActualizar)
                .pedidoId(101L)
                .repartidorAsignado("Carlos Gomez Modificado")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada)
                .estado("EN_TRANSITO")
                .build();

        // Configurar mock con any()
        when(despachoService.actualizarDespacho(any(Long.class), any(DespachoRequestDTO.class)))
                .thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PUT con el ID en la URL y el JSON en el body
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/despachos/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y campos actualizados
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.repartidorAsignado").value("Carlos Gomez Modificado"))
                .andExpect(jsonPath("$.estado").value("EN_TRANSITO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(despachoService, times(1)).actualizarDespacho(any(Long.class), any(DespachoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el estado no se actualizara y el service retornara el estado viejo ("PROGRAMADO"),
         * el test fallaría con:
         *
         *   Expected: $.estado == "EN_TRANSITO"
         *   But was:  "PROGRAMADO"
         *
         * QA debería reportar:
         *   - Endpoint: PUT /api/despachos/1
         *   - Se esperaba: JSON con estado = "EN_TRANSITO"
         *   - Se obtuvo:   JSON con estado = "PROGRAMADO"
         *   - Causa probable: El método actualizarDespacho() en el service no está
         *     guardando el nuevo estado del request.
         */
    }

    // =========================================================
    // TEST 6: PATCH /api/despachos/{id}/estado — cambiarEstado()
    // =========================================================

    @Test
    @DisplayName("PATCH /api/despachos/{id}/estado - debería cambiar el estado y retornar HTTP 200")
    void cambiarEstado_cuandoDatosValidos_deberiaRetornar200ConEstadoActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, nuevo estado y respuesta simulada
        // --------------------------------------------------
        Long idDespacho = 1L;
        String nuevoEstado = "ENTREGADO";
        LocalDateTime fechaEstimada = LocalDateTime.of(2026, 6, 29, 21, 30);

        DespachoResponseDTO response = DespachoResponseDTO.builder()
                .id(idDespacho)
                .pedidoId(101L)
                .repartidorAsignado("Carlos Gomez")
                .direccionEntrega("Av. Providencia 1234, Santiago")
                .fechaEntregaEstimada(fechaEstimada)
                .estado(nuevoEstado)
                .build();

        // Configurar mock
        when(despachoService.cambiarEstado(idDespacho, nuevoEstado)).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PATCH con @RequestParam
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/despachos/{id}/estado", idDespacho)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y que el estado cambió
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(despachoService, times(1)).cambiarEstado(idDespacho, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller tuviera un error al enlazar el @RequestParam y no mapeara
         * el parámetro "estado", el test fallaría con:
         *
         *   Expected: 200
         *   But was:  400 Bad Request
         *
         * QA debería reportar:
         *   - Endpoint: PATCH /api/despachos/1/estado?estado=ENTREGADO
         *   - Se esperaba: HTTP 200 OK con el estado actualizado
         *   - Se obtuvo:   HTTP 400 Bad Request
         *   - Causa probable: El parámetro "estado" no hace match con el esperado
         *     en el método cambiarEstado del controller.
         */
    }

    // =========================================================
    // TEST 7: DELETE /api/despachos/{id} — eliminar()
    // =========================================================

    @Test
    @DisplayName("DELETE /api/despachos/{id} - debería eliminar el despacho y retornar HTTP 204")
    void eliminar_cuandoIdExiste_deberiaRetornar204SinContenido() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID del despacho a eliminar
        // --------------------------------------------------
        Long idEliminar = 1L;

        // eliminarDespacho() es void → usamos doNothing() para simular éxito
        org.mockito.Mockito.doNothing().when(despachoService).eliminarDespacho(idEliminar);

        // --------------------------------------------------
        // ACT: ejecutar la petición DELETE con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/despachos/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 204 y que el body esté vacío
                // --------------------------------------------------
                .andExpect(status().isNoContent())       // HTTP 204 No Content
                .andExpect(content().string(""));        // Body completamente vacío

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(despachoService, times(1)).eliminarDespacho(idEliminar);

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
         *   - Endpoint: DELETE /api/despachos/1
         *   - Se esperaba: HTTP 204 No Content (body vacío)
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: El developer usó ResponseEntity.ok().build()
         *     en lugar de ResponseEntity.noContent().build().
         */
    }
}







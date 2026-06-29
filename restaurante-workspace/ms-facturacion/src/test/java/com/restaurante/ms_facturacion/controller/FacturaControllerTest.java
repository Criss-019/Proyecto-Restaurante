package com.restaurante.ms_facturacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_facturacion.dto.FacturaRequestDTO;
import com.restaurante.ms_facturacion.dto.FacturaResponseDTO;
import com.restaurante.ms_facturacion.service.FacturaService;
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
 * Pruebas unitarias para FacturaController.
 *
 * Estrategia:
 *   - FacturaController es REAL (instanciado por @WebMvcTest)
 *   - FacturaService es SIMULADO con @MockBean (no toca BD ni Eureka)
 *   - MockMvc simula las peticiones HTTP (GET, POST, PUT, PATCH, DELETE)
 */
@WebMvcTest(FacturaController.class)
class FacturaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FacturaService facturaService;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1: POST /api/facturacion — emitir()
    // =========================================================

    @Test
    @DisplayName("POST /api/facturacion - debería emitir factura y retornar HTTP 201")
    void emitir_cuandoDatosValidos_deberiaRetornar201() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar datos de entrada y respuesta mock
        // --------------------------------------------------
        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setPedidoId(101L);
        request.setSubtotal(10000.0);

        FacturaResponseDTO response = FacturaResponseDTO.builder()
                .id(1L)
                .pedidoId(101L)
                .numeroFactura("FAC-00001")
                .subtotal(10000.0)
                .impuestos(1900.0)
                .total(11900.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("http://restaurante.com/pdf/FAC-00001.pdf")
                .build();

        // Cuando el service reciba cualquier FacturaRequestDTO, devuelve la respuesta simulada
        when(facturaService.emitirFactura(any(FacturaRequestDTO.class))).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición POST con MockMvc
        // --------------------------------------------------
        mockMvc.perform(post("/api/facturacion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar estado HTTP y campos del JSON devuelto
                // --------------------------------------------------
                .andExpect(status().isCreated())                         // HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.numeroFactura").value("FAC-00001"))
                .andExpect(jsonPath("$.subtotal").value(10000.0))
                .andExpect(jsonPath("$.impuestos").value(1900.0))
                .andExpect(jsonPath("$.total").value(11900.0))
                .andExpect(jsonPath("$.estadoFiscal").value("EMITIDA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(facturaService, times(1)).emitirFactura(any(FacturaRequestDTO.class));

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
         *   - Endpoint: POST /api/facturacion
         *   - Se esperaba: HTTP 201 Created
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok() en lugar de
         *     new ResponseEntity<>(response, HttpStatus.CREATED)
         */
    }

    // =========================================================
    // TEST 2: GET /api/facturacion — listar()
    // =========================================================

    @Test
    @DisplayName("GET /api/facturacion - debería retornar lista de facturas y HTTP 200")
    void listar_cuandoExistenFacturas_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar lista simulada con 2 facturas
        // --------------------------------------------------
        FacturaResponseDTO factura1 = FacturaResponseDTO.builder()
                .id(1L)
                .pedidoId(101L)
                .numeroFactura("FAC-00001")
                .subtotal(10000.0)
                .impuestos(1900.0)
                .total(11900.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .build();

        FacturaResponseDTO factura2 = FacturaResponseDTO.builder()
                .id(2L)
                .pedidoId(102L)
                .numeroFactura("FAC-00002")
                .subtotal(20000.0)
                .impuestos(3800.0)
                .total(23800.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("PAGADA")
                .build();

        List<FacturaResponseDTO> lista = Arrays.asList(factura1, factura2);

        // Cuando el service sea llamado, devuelve la lista simulada
        when(facturaService.obtenerTodas()).thenReturn(lista);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET sin parámetros
        // --------------------------------------------------
        mockMvc.perform(get("/api/facturacion")
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].numeroFactura").value("FAC-00001"))
                .andExpect(jsonPath("$[0].estadoFiscal").value("EMITIDA"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].numeroFactura").value("FAC-00002"))
                .andExpect(jsonPath("$[1].estadoFiscal").value("PAGADA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(facturaService, times(1)).obtenerTodas();

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
         *   - Endpoint: GET /api/facturacion
         *   - Se esperaba: JSON array con 2 facturas
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: el repositorio no está retornando los datos correctos
         *     o el mock del service no fue configurado antes de ejecutar el test.
         */
    }

    // =========================================================
    // TEST 3: GET /api/facturacion/{id} — obtenerPorId()
    // =========================================================

    @Test
    @DisplayName("GET /api/facturacion/{id} - debería retornar la factura encontrada y HTTP 200")
    void obtenerPorId_cuandoIdExiste_deberiaRetornar200ConFactura() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el id y la factura simulada
        // --------------------------------------------------
        Long idBuscado = 1L;

        FacturaResponseDTO facturaEncontrada = FacturaResponseDTO.builder()
                .id(idBuscado)
                .pedidoId(101L)
                .numeroFactura("FAC-00001")
                .subtotal(10000.0)
                .impuestos(1900.0)
                .total(11900.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("http://restaurante.com/pdf/FAC-00001.pdf")
                .build();

        // Cuando el service reciba el id=1, devuelve la factura simulada
        when(facturaService.obtenerPorId(idBuscado)).thenReturn(facturaEncontrada);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/facturacion/{id}", idBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos de la factura devuelta
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.numeroFactura").value("FAC-00001"))
                .andExpect(jsonPath("$.total").value(11900.0))
                .andExpect(jsonPath("$.estadoFiscal").value("EMITIDA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(facturaService, times(1)).obtenerPorId(idBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service lanzara una excepción por no encontrar la factura,
         * y no estuviera manejada, el test fallaría con:
         *
         *   Expected: 200
         *   But was:  500
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/facturacion/1
         *   - Se esperaba: HTTP 200 OK con la factura
         *   - Se obtuvo:   HTTP 500 Internal Server Error
         *   - Causa probable: Falta implementar un handler de excepciones que
         *     capture el EntityNotFoundException y retorne HTTP 404.
         */
    }

    // =========================================================
    // TEST 4: GET /api/facturacion/pedido/{pedidoId} — obtenerPorPedidoId()
    // =========================================================

    @Test
    @DisplayName("GET /api/facturacion/pedido/{pedidoId} - debería retornar la factura del pedido y HTTP 200")
    void obtenerPorPedidoId_cuandoPedidoExiste_deberiaRetornar200ConFactura() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el pedidoId y la factura simulada
        // --------------------------------------------------
        Long pedidoIdBuscado = 101L;

        FacturaResponseDTO facturaEncontrada = FacturaResponseDTO.builder()
                .id(1L)
                .pedidoId(pedidoIdBuscado)
                .numeroFactura("FAC-00001")
                .subtotal(10000.0)
                .impuestos(1900.0)
                .total(11900.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("http://restaurante.com/pdf/FAC-00001.pdf")
                .build();

        // Cuando el service reciba el pedidoId=101, devuelve la factura simulada
        when(facturaService.obtenerPorPedidoId(pedidoIdBuscado)).thenReturn(facturaEncontrada);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el pedidoId en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/facturacion/pedido/{pedidoId}", pedidoIdBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y campos de la factura devuelta
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(101))
                .andExpect(jsonPath("$.numeroFactura").value("FAC-00001"))
                .andExpect(jsonPath("$.estadoFiscal").value("EMITIDA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el pedidoId correcto
        // --------------------------------------------------
        verify(facturaService, times(1)).obtenerPorPedidoId(pedidoIdBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el controller tuviera una ruta mapeada incorrectamente o no pasara
         * el pedidoId al service, el test fallaría. Si no encontrara la factura
         * y retornara una respuesta vacía o error, fallaría con:
         *
         *   Expected: 200
         *   But was:  404 (o 500)
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/facturacion/pedido/101
         *   - Se esperaba: HTTP 200 OK con el cuerpo de la factura
         *   - Se obtuvo:   HTTP 404 Not Found (o 500)
         *   - Causa probable: El mapping del endpoint no es correcto, o la llamada
         *     al service no está configurada de forma adecuada en el controller.
         */
    }

    // =========================================================
    // TEST 5: PUT /api/facturacion/{id} — actualizar()
    // =========================================================

    @Test
    @DisplayName("PUT /api/facturacion/{id} - debería actualizar la factura y retornar HTTP 200")
    void actualizar_cuandoDatosValidos_deberiaRetornar200ConFacturaActualizada() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, request body y response simulado
        // --------------------------------------------------
        Long idActualizar = 1L;

        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setPedidoId(101L);
        request.setSubtotal(12000.0); // Se actualizó el subtotal (ej: por cambio en pedido)

        FacturaResponseDTO response = FacturaResponseDTO.builder()
                .id(idActualizar)
                .pedidoId(101L)
                .numeroFactura("FAC-00001")
                .subtotal(12000.0)
                .impuestos(2280.0) // 19% de 12000
                .total(14280.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("http://restaurante.com/pdf/FAC-00001.pdf")
                .build();

        // Configurar mock con any()
        when(facturaService.actualizarFactura(any(Long.class), any(FacturaRequestDTO.class)))
                .thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PUT con el ID en la URL y el JSON en el body
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/facturacion/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y campos actualizados
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.subtotal").value(12000.0))
                .andExpect(jsonPath("$.total").value(14280.0))
                .andExpect(jsonPath("$.estadoFiscal").value("EMITIDA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(facturaService, times(1)).actualizarFactura(any(Long.class), any(FacturaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el subtotal no se actualizara y el service retornara el subtotal viejo (10000.0),
         * el test fallaría con:
         *
         *   Expected: $.subtotal == 12000.0
         *   But was:  10000.0
         *
         * QA debería reportar:
         *   - Endpoint: PUT /api/facturacion/1
         *   - Se esperaba: JSON con subtotal = 12000.0
         *   - Se obtuvo:   JSON con subtotal = 10000.0
         *   - Causa probable: El método actualizarFactura() en el service no está
         *     guardando o recalculando el nuevo subtotal del request.
         */
    }

    // =========================================================
    // TEST 6: PATCH /api/facturacion/{id}/estado — cambiarEstadoFiscal()
    // =========================================================

    @Test
    @DisplayName("PATCH /api/facturacion/{id}/estado - debería cambiar el estado fiscal y retornar HTTP 200")
    void cambiarEstadoFiscal_cuandoDatosValidos_deberiaRetornar200ConEstadoFiscalActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, nuevo estado fiscal y respuesta simulada
        // --------------------------------------------------
        Long idFactura = 1L;
        String nuevoEstadoFiscal = "PAGADA";

        FacturaResponseDTO response = FacturaResponseDTO.builder()
                .id(idFactura)
                .pedidoId(101L)
                .numeroFactura("FAC-00001")
                .subtotal(10000.0)
                .impuestos(1900.0)
                .total(11900.0)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal(nuevoEstadoFiscal)
                .build();

        // Configurar mock
        when(facturaService.cambiarEstadoFiscal(idFactura, nuevoEstadoFiscal)).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición PATCH con @RequestParam
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/facturacion/{id}/estado", idFactura)
                        .param("estado", nuevoEstadoFiscal)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y que el estado fiscal cambió
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estadoFiscal").value("PAGADA"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(facturaService, times(1)).cambiarEstadoFiscal(idFactura, nuevoEstadoFiscal);

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
         *   - Endpoint: PATCH /api/facturacion/1/estado?estado=PAGADA
         *   - Se esperaba: HTTP 200 OK con el estado fiscal actualizado
         *   - Se obtuvo:   HTTP 400 Bad Request
         *   - Causa probable: El parámetro "estado" no hace match con el esperado
         *     en el método cambiarEstadoFiscal del controller.
         */
    }

    // =========================================================
    // TEST 7: DELETE /api/facturacion/{id} — eliminar()
    // =========================================================

    @Test
    @DisplayName("DELETE /api/facturacion/{id} - debería eliminar la factura y retornar HTTP 204")
    void eliminar_cuandoIdExiste_deberiaRetornar204SinContenido() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID de la factura a eliminar
        // --------------------------------------------------
        Long idEliminar = 1L;

        // eliminarFactura() es void → usamos doNothing() para simular éxito
        org.mockito.Mockito.doNothing().when(facturaService).eliminarFactura(idEliminar);

        // --------------------------------------------------
        // ACT: ejecutar la petición DELETE con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/facturacion/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 204 y que el body esté vacío
                // --------------------------------------------------
                .andExpect(status().isNoContent())       // HTTP 204 No Content
                .andExpect(content().string(""));        // Body completamente vacío

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(facturaService, times(1)).eliminarFactura(idEliminar);

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
         *   - Endpoint: DELETE /api/facturacion/1
         *   - Se esperaba: HTTP 204 No Content (body vacío)
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: El developer usó ResponseEntity.ok().build()
         *     en lugar de ResponseEntity.noContent().build().
         */
    }
}







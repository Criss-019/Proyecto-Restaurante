package com.restaurante.ms_pagos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurante.ms_pagos.dto.PagoRequestDTO;
import com.restaurante.ms_pagos.dto.PagoResponseDTO;
import com.restaurante.ms_pagos.service.PagoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para PagoController.
 * 
 * Estrategia:
 * - PagoController es REAL (instanciado por @WebMvcTest).
 * - PagoService es SIMULADO con @MockBean.
 * - MockMvc simula peticiones HTTP.
 */
@WebMvcTest(PagoController.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagoService pagoService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/pagos - Debe registrar un pago y retornar HTTP 201 Created")
    void registrar_DebeRetornar201Created_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar datos de entrada y respuesta simulada
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(50L);
        request.setMonto(25000.0);
        request.setMetodoPago("TARJETA_CREDITO");

        PagoResponseDTO responseSimulado = PagoResponseDTO.builder()
                .id(1L)
                .pedidoId(50L)
                .monto(25000.0)
                .metodoPago("TARJETA_CREDITO")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoService.registrarPago(any(PagoRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP POST
        mockMvc.perform(post("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar el estado HTTP 201 Created y los atributos devueltos en el JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(50))
                .andExpect(jsonPath("$.monto").value(25000.0))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA_CREDITO"))
                .andExpect(jsonPath("$.estado").value("APROBADO"));

        // VERIFY: Comprobar que el servicio fue llamado exactamente 1 vez
        verify(pagoService, times(1)).registrarPago(any(PagoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 201 Created se obtiene HTTP 400 Bad Request:
         * - QA debe reportar: "Falla al registrar pago: La API responde 400 Bad Request al enviar PagoRequestDTO válido."
         * - Desarrollo debe revisar: Las anotaciones de validación (@NotNull, @Positive, @NotBlank) en PagoRequestDTO.
         */
    }

    @Test
    @DisplayName("GET /api/pagos - Debe retornar lista de pagos con HTTP 200 OK")
    void listar_DebeRetornar200OK_YListaDePagos() throws Exception {
        // ARRANGE: Preparar lista simulada de pagos
        PagoResponseDTO pago1 = PagoResponseDTO.builder()
                .id(1L)
                .pedidoId(50L)
                .monto(15000.0)
                .metodoPago("EFECTIVO")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        PagoResponseDTO pago2 = PagoResponseDTO.builder()
                .id(2L)
                .pedidoId(51L)
                .monto(32000.0)
                .metodoPago("TARJETA_DEBITO")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoService.obtenerTodos()).thenReturn(java.util.Arrays.asList(pago1, pago2));

        // ACT: Ejecutar la petición HTTP GET
        mockMvc.perform(get("/api/pagos")
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y la estructura de la lista JSON devuelta
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].metodoPago").value("EFECTIVO"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].metodoPago").value("TARJETA_DEBITO"));

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez
        verify(pagoService, times(1)).obtenerTodos();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla al listar pagos: El servidor responde 500 Internal Server Error."
         * - Desarrollo debe revisar: Excepciones en PagoService.obtenerTodos() o problemas en la conversión Entidad-DTO.
         */
    }

    @Test
    @DisplayName("GET /api/pagos/{id} - Debe retornar un pago existente con HTTP 200 OK")
    void obtenerPorId_DebeRetornar200OK_CuandoPagoExiste() throws Exception {
        // ARRANGE: Preparar datos simulados para un pago con ID 1L
        Long idBusqueda = 1L;
        PagoResponseDTO responseSimulado = PagoResponseDTO.builder()
                .id(idBusqueda)
                .pedidoId(50L)
                .monto(25000.0)
                .metodoPago("TARJETA_CREDITO")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoService.obtenerPorId(idBusqueda)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP GET con el ID en el path
        mockMvc.perform(get("/api/pagos/{id}", idBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y coincidencia de campos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.pedidoId").value(50))
                .andExpect(jsonPath("$.monto").value(25000.0))
                .andExpect(jsonPath("$.metodoPago").value("TARJETA_CREDITO"));

        // VERIFY: Comprobar que el servicio fue llamado con el ID correcto
        verify(pagoService, times(1)).obtenerPorId(idBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 404 Not Found para un pago registrado:
         * - QA debe reportar: "Falla al buscar pago por ID: La API devuelve 404 Not Found cuando el pago sí existe."
         * - Desarrollo debe revisar: La búsqueda por id en PagoService.obtenerPorId() o la anotación @PathVariable.
         */
    }

    @Test
    @DisplayName("GET /api/pagos/pedido/{pedidoId} - Debe retornar los pagos asociados al pedido con HTTP 200 OK")
    void obtenerPorPedidoId_DebeRetornar200OK_CuandoPedidoTienePagos() throws Exception {
        // ARRANGE: Preparar lista simulada de pagos para un pedidoId 50L
        Long pedidoIdBusqueda = 50L;
        PagoResponseDTO pago1 = PagoResponseDTO.builder()
                .id(1L)
                .pedidoId(pedidoIdBusqueda)
                .monto(10000.0)
                .metodoPago("EFECTIVO")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoService.obtenerPorPedidoId(pedidoIdBusqueda))
                .thenReturn(java.util.Collections.singletonList(pago1));

        // ACT: Ejecutar la petición HTTP GET con el pedidoId en el path
        mockMvc.perform(get("/api/pagos/pedido/{pedidoId}", pedidoIdBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia del arreglo JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pedidoId").value(50))
                .andExpect(jsonPath("$[0].monto").value(10000.0));

        // VERIFY: Comprobar que el servicio fue invocado con el pedidoId correcto
        verify(pagoService, times(1)).obtenerPorPedidoId(pedidoIdBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene un arreglo vacío cuando el pedido sí tiene pagos:
         * - QA debe reportar: "Falla al listar pagos por pedidoId: Se retorna un arreglo vacío [] para un pedido con pagos asociados."
         * - Desarrollo debe revisar: La consulta JPA por pedidoId en PagoService.obtenerPorPedidoId().
         */
    }

    @Test
    @DisplayName("PUT /api/pagos/{id} - Debe actualizar un pago y retornar HTTP 200 OK")
    void actualizar_DebeRetornar200OK_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar ID, request con datos modificados y respuesta simulada
        Long idActualizar = 1L;
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(50L);
        request.setMonto(30000.0);
        request.setMetodoPago("TRANSFERENCIA");

        PagoResponseDTO responseSimulado = PagoResponseDTO.builder()
                .id(idActualizar)
                .pedidoId(50L)
                .monto(30000.0)
                .metodoPago("TRANSFERENCIA")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoService.actualizarPago(eq(idActualizar), any(PagoRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PUT enviando el ID en la URL y el cuerpo JSON
        mockMvc.perform(put("/api/pagos/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar código HTTP 200 OK y que los datos devueltos reflejen la actualización
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.monto").value(30000.0))
                .andExpect(jsonPath("$.metodoPago").value("TRANSFERENCIA"));

        // VERIFY: Comprobar que el servicio fue llamado con el ID y DTO correspondientes
        verify(pagoService, times(1)).actualizarPago(eq(idActualizar), any(PagoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por datos inválidos en el request:
         * - QA debe reportar: "Falla al actualizar pago: La API devuelve 400 Bad Request al enviar datos válidos de actualización."
         * - Desarrollo debe revisar: Las validaciones Bean Validation en PagoRequestDTO o la anotación @Valid en el Controller.
         */
    }

    @Test
    @DisplayName("PATCH /api/pagos/{id}/estado - Debe actualizar el estado del pago con HTTP 200 OK")
    void cambiarEstado_DebeRetornar200OK_CuandoParametrosSonValidos() throws Exception {
        // ARRANGE: Preparar ID y nuevo estado simulado
        Long idPago = 1L;
        String nuevoEstado = "RECHAZADO";

        PagoResponseDTO responseSimulado = PagoResponseDTO.builder()
                .id(idPago)
                .pedidoId(50L)
                .monto(25000.0)
                .metodoPago("TARJETA_CREDITO")
                .fechaPago(LocalDateTime.now())
                .estado(nuevoEstado)
                .build();

        when(pagoService.cambiarEstado(idPago, nuevoEstado)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PATCH enviando el param estado
        mockMvc.perform(patch("/api/pagos/{id}/estado", idPago)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia del estado actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("RECHAZADO"));

        // VERIFY: Comprobar que el servicio fue invocado con los parámetros correctos
        verify(pagoService, times(1)).cambiarEstado(idPago, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request al omitir el query param 'estado':
         * - QA debe reportar: "Falla al cambiar estado de pago: La API devuelve 400 Bad Request por falta de parámetro requerido."
         * - Desarrollo debe revisar: La anotación @RequestParam String estado en PagoController.
         */
    }

    @Test
    @DisplayName("DELETE /api/pagos/{id} - Debe eliminar el pago y retornar HTTP 204 No Content")
    void eliminar_DebeRetornar204NoContent_CuandoPagoExiste() throws Exception {
        // ARRANGE: Preparar ID a eliminar y mockear servicio void
        Long idEliminar = 1L;
        doNothing().when(pagoService).eliminarPago(idEliminar);

        // ACT: Ejecutar la petición HTTP DELETE
        mockMvc.perform(delete("/api/pagos/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar que la respuesta sea HTTP 204 No Content
                .andExpect(status().isNoContent());

        // VERIFY: Comprobar invocación del servicio con el ID a eliminar
        verify(pagoService, times(1)).eliminarPago(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 204 No Content se obtiene HTTP 200 OK o HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla en eliminación de pago: Se esperaba HTTP 204 No Content pero la API devolvió otro código."
         * - Desarrollo debe revisar: El retorno del método eliminar en PagoController (ResponseEntity.noContent().build()).
         */
    }
}

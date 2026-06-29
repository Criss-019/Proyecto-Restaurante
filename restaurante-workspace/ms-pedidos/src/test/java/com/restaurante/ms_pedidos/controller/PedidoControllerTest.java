package com.restaurante.ms_pedidos.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import com.restaurante.ms_pedidos.service.PedidoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para PedidoController.
 * 
 * Estrategia:
 * - PedidoController es REAL (instanciado por @WebMvcTest).
 * - PedidoService es SIMULADO con @MockBean.
 * - MockMvc simula peticiones HTTP.
 */
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/pedidos - Debe crear un pedido y retornar HTTP 201 Created")
    void crear_DebeRetornar201Created_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar datos de entrada y respuesta simulada
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(10L);
        request.setTipoEntrega("MESA");

        PedidoResponseDTO responseSimulado = PedidoResponseDTO.builder()
                .id(1L)
                .clienteId(10L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("MESA")
                .estado("PENDIENTE")
                .total(15000.0)
                .build();

        when(pedidoService.crearPedido(any(PedidoRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP POST
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar el estado HTTP 201 Created y los valores del cuerpo JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.tipoEntrega").value("MESA"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(15000.0));

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez
        verify(pedidoService, times(1)).crearPedido(any(PedidoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 201 Created se obtiene HTTP 400 Bad Request:
         * - QA debe reportar: "Falla al crear pedido: La API responde 400 Bad Request al enviar un PedidoRequestDTO válido."
         * - Desarrollo debe revisar: Las anotaciones @NotNull y @NotBlank en PedidoRequestDTO o el manejo en el Controller.
         */
    }

    @Test
    @DisplayName("GET /api/pedidos - Debe retornar lista de pedidos con HTTP 200 OK")
    void listar_DebeRetornar200OK_YListaDePedidos() throws Exception {
        // ARRANGE: Preparar lista simulada de pedidos
        PedidoResponseDTO pedido1 = PedidoResponseDTO.builder()
                .id(1L)
                .clienteId(10L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("MESA")
                .estado("PENDIENTE")
                .total(12000.0)
                .build();

        PedidoResponseDTO pedido2 = PedidoResponseDTO.builder()
                .id(2L)
                .clienteId(11L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("DELIVERY")
                .estado("COMPLETADO")
                .total(25000.0)
                .build();

        when(pedidoService.obtenerTodos()).thenReturn(java.util.Arrays.asList(pedido1, pedido2));

        // ACT: Ejecutar la petición HTTP GET
        mockMvc.perform(get("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y estructura del arreglo JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].tipoEntrega").value("MESA"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].tipoEntrega").value("DELIVERY"));

        // VERIFY: Comprobar invocación única al servicio
        verify(pedidoService, times(1)).obtenerTodos();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla al listar pedidos: El servidor respondió 500 Internal Server Error."
         * - Desarrollo debe revisar: Excepciones no controladas en PedidoService.obtenerTodos() o en el mapeo de entidades.
         */
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} - Debe retornar un pedido existente con HTTP 200 OK")
    void obtenerPorId_DebeRetornar200OK_CuandoPedidoExiste() throws Exception {
        // ARRANGE: Preparar datos simulados para un pedido con ID 1L
        Long idBusqueda = 1L;
        PedidoResponseDTO responseSimulado = PedidoResponseDTO.builder()
                .id(idBusqueda)
                .clienteId(10L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("MESA")
                .estado("EN_PREPARACION")
                .total(18500.0)
                .build();

        when(pedidoService.obtenerPorId(idBusqueda)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP GET con el ID en la URL
        mockMvc.perform(get("/api/pedidos/{id}", idBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y la coincidencia de atributos en el JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(10))
                .andExpect(jsonPath("$.tipoEntrega").value("MESA"))
                .andExpect(jsonPath("$.estado").value("EN_PREPARACION"))
                .andExpect(jsonPath("$.total").value(18500.0));

        // VERIFY: Comprobar que el servicio fue invocado con el ID correcto
        verify(pedidoService, times(1)).obtenerPorId(idBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 404 Not Found cuando el pedido existe:
         * - QA debe reportar: "Falla al buscar pedido por ID: Se retorna 404 Not Found para un ID existente."
         * - Desarrollo debe revisar: La anotación @PathVariable Long id o el método PedidoService.obtenerPorId().
         */
    }

    @Test
    @DisplayName("GET /api/pedidos/cliente/{clienteId} - Debe retornar los pedidos asociados al cliente con HTTP 200 OK")
    void obtenerPorClienteId_DebeRetornar200OK_CuandoClienteTienePedidos() throws Exception {
        // ARRANGE: Preparar lista simulada de pedidos para clienteId 10L
        Long clienteIdBusqueda = 10L;
        PedidoResponseDTO pedido1 = PedidoResponseDTO.builder()
                .id(1L)
                .clienteId(clienteIdBusqueda)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("MESA")
                .estado("PENDIENTE")
                .total(12000.0)
                .build();

        when(pedidoService.obtenerPorClienteId(clienteIdBusqueda))
                .thenReturn(java.util.Collections.singletonList(pedido1));

        // ACT: Ejecutar la petición HTTP GET con el clienteId en la URL
        mockMvc.perform(get("/api/pedidos/cliente/{clienteId}", clienteIdBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y estructura del JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(10))
                .andExpect(jsonPath("$[0].total").value(12000.0));

        // VERIFY: Comprobar que el servicio fue llamado con el clienteId correcto
        verify(pedidoService, times(1)).obtenerPorClienteId(clienteIdBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene un arreglo vacío cuando el cliente tiene pedidos:
         * - QA debe reportar: "Falla al listar pedidos por clienteId: Se retorna un arreglo vacío [] para un cliente con pedidos registrados."
         * - Desarrollo debe revisar: La consulta JPA por clienteId en PedidoService.obtenerPorClienteId().
         */
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id} - Debe actualizar un pedido y retornar HTTP 200 OK")
    void actualizar_DebeRetornar200OK_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar ID, solicitud con datos actualizados y respuesta simulada
        Long idActualizar = 1L;
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(10L);
        request.setTipoEntrega("DELIVERY");

        PedidoResponseDTO responseSimulado = PedidoResponseDTO.builder()
                .id(idActualizar)
                .clienteId(10L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("DELIVERY")
                .estado("PENDIENTE")
                .total(15000.0)
                .build();

        when(pedidoService.actualizarPedido(eq(idActualizar), any(PedidoRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PUT enviando el ID en la URL y el JSON en el cuerpo
        mockMvc.perform(put("/api/pedidos/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia de datos actualizados
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.tipoEntrega").value("DELIVERY"));

        // VERIFY: Comprobar invocación única del servicio con los parámetros correspondientes
        verify(pedidoService, times(1)).actualizarPedido(eq(idActualizar), any(PedidoRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por validación:
         * - QA debe reportar: "Falla en actualización de pedido: La API responde 400 Bad Request enviando un PedidoRequestDTO válido."
         * - Desarrollo debe revisar: Las validaciones Bean Validation (@NotBlank, @NotNull) en PedidoRequestDTO.
         */
    }

    @Test
    @DisplayName("PUT /api/pedidos/{id}/estado - Debe actualizar el estado del pedido con HTTP 200 OK")
    void cambiarEstado_DebeRetornar200OK_CuandoParametrosSonValidos() throws Exception {
        // ARRANGE: Preparar ID y nuevo estado simulado
        Long idPedido = 1L;
        String nuevoEstado = "ENTREGADO";

        PedidoResponseDTO responseSimulado = PedidoResponseDTO.builder()
                .id(idPedido)
                .clienteId(10L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("MESA")
                .estado(nuevoEstado)
                .total(15000.0)
                .build();

        when(pedidoService.cambiarEstado(idPedido, nuevoEstado)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PUT enviando el parámetro estado
        mockMvc.perform(put("/api/pedidos/{id}/estado", idPedido)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia del estado actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));

        // VERIFY: Comprobar invocación del servicio con los parámetros exactos
        verify(pedidoService, times(1)).cambiarEstado(idPedido, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 405 Method Not Allowed al intentar usar PATCH:
         * - QA debe reportar: "Falla en cambio de estado: El endpoint está configurado con PUT en lugar de PATCH."
         * - Desarrollo debe revisar: La anotación HTTP (@PutMapping vs @PatchMapping) en PedidoController.
         */
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} - Debe eliminar el pedido y retornar HTTP 204 No Content")
    void eliminar_DebeRetornar204NoContent_CuandoPedidoExiste() throws Exception {
        // ARRANGE: Preparar ID a eliminar y mockear comportamiento void del servicio
        Long idEliminar = 1L;
        doNothing().when(pedidoService).eliminarPedido(idEliminar);

        // ACT: Ejecutar la petición HTTP DELETE
        mockMvc.perform(delete("/api/pedidos/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar que la respuesta sea HTTP 204 No Content
                .andExpect(status().isNoContent());

        // VERIFY: Comprobar invocación del servicio con el ID correspondiente
        verify(pedidoService, times(1)).eliminarPedido(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 204 No Content se obtiene HTTP 200 OK o HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla en eliminación de pedido: Se esperaba HTTP 204 No Content pero la API respondió con otro código."
         * - Desarrollo debe revisar: El retorno del método eliminar en PedidoController (ResponseEntity.noContent().build()).
         */
    }
}

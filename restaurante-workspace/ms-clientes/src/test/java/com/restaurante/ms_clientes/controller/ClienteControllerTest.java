package com.restaurante.ms_clientes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurante.ms_clientes.dto.ClienteRequestDTO;
import com.restaurante.ms_clientes.dto.ClienteResponseDTO;
import com.restaurante.ms_clientes.service.ClienteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para ClienteController.
 *
 * Estrategia:
 *   - ClienteController es REAL (instanciado por @WebMvcTest)
 *   - ClienteService es SIMULADO con @MockBean (no toca BD ni Eureka)
 *   - MockMvc simula las peticiones HTTP (GET, POST, PUT, DELETE)
 */
@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ClienteService simulado: no llama a la BD real
    @MockBean
    private ClienteService clienteService;

    // ObjectMapper para convertir objetos Java a JSON
    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // TEST 1: POST /api/clientes — crear()
    // =========================================================

    @Test
    @DisplayName("POST /api/clientes - debería crear un cliente y retornar HTTP 201")
    void crear_cuandoDatosValidos_deberiaRetornar201() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar datos de entrada y respuesta mock
        // --------------------------------------------------
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNombre("Juan Pérez");
        request.setEmail("juan.perez@email.com");
        request.setTelefono("+56912345678");
        request.setDireccion("Av. Siempre Viva 742");

        ClienteResponseDTO response = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 742")
                .build();

        // Cuando el service reciba cualquier ClienteRequestDTO, devuelve la respuesta simulada
        when(clienteService.crearCliente(any(ClienteRequestDTO.class))).thenReturn(response);

        // --------------------------------------------------
        // ACT: ejecutar la petición POST con MockMvc
        // --------------------------------------------------
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // --------------------------------------------------
                // ASSERT: verificar estado HTTP y campos del JSON devuelto
                // --------------------------------------------------
                .andExpect(status().isCreated())                         // HTTP 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$.telefono").value("+56912345678"))
                .andExpect(jsonPath("$.direccion").value("Av. Siempre Viva 742"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(clienteService, times(1)).crearCliente(any(ClienteRequestDTO.class));

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
         *   - Endpoint: POST /api/clientes
         *   - Se esperaba: HTTP 201 Created
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok() en lugar de
         *     new ResponseEntity<>(response, HttpStatus.CREATED)
         */
    }

    // =========================================================
    // TEST 2: GET /api/clientes — listar()
    // =========================================================

    @Test
    @DisplayName("GET /api/clientes - debería retornar lista de clientes y HTTP 200")
    void listar_cuandoExistenClientes_deberiaRetornar200ConLista() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar lista simulada con 2 clientes
        // --------------------------------------------------
        ClienteResponseDTO cliente1 = ClienteResponseDTO.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 742")
                .build();

        ClienteResponseDTO cliente2 = ClienteResponseDTO.builder()
                .id(2L)
                .nombre("María González")
                .email("maria.gonzalez@email.com")
                .telefono("+56987654321")
                .direccion("Calle Los Olivos 123")
                .build();

        List<ClienteResponseDTO> listaClientes = Arrays.asList(cliente1, cliente2);

        // Cuando el service sea llamado, devuelve la lista simulada
        when(clienteService.obtenerTodos()).thenReturn(listaClientes);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET sin parámetros
        // --------------------------------------------------
        mockMvc.perform(get("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y contenido del array JSON
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))              // La lista tiene 2 elementos
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$[0].email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("María González"))
                .andExpect(jsonPath("$[1].email").value("maria.gonzalez@email.com"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez
        // --------------------------------------------------
        verify(clienteService, times(1)).obtenerTodos();

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el service devolviera una lista vacía cuando debería haber clientes,
         * el test fallaría con:
         *
         *   Expected: $.length() == 2
         *   But was:  0
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/clientes
         *   - Se esperaba: JSON array con 2 clientes
         *   - Se obtuvo:   JSON array vacío []
         *   - Causa probable: el repositorio no está persistiendo los datos,
         *     o el mock del service no fue configurado antes de ejecutar el test
         */
    }

    // =========================================================
    // TEST 3: GET /api/clientes/{id} — obtenerPorId()
    // =========================================================

    @Test
    @DisplayName("GET /api/clientes/{id} - debería retornar el cliente encontrado y HTTP 200")
    void obtenerPorId_cuandoIdExiste_deberiaRetornar200ConCliente() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el cliente simulado que devolverá el service
        // --------------------------------------------------
        Long idBuscado = 1L;

        ClienteResponseDTO clienteEncontrado = ClienteResponseDTO.builder()
                .id(idBuscado)
                .nombre("Juan Pérez")
                .email("juan.perez@email.com")
                .telefono("+56912345678")
                .direccion("Av. Siempre Viva 742")
                .build();

        // Cuando el service reciba el id=1, devuelve el cliente simulado
        when(clienteService.obtenerPorId(idBuscado)).thenReturn(clienteEncontrado);

        // --------------------------------------------------
        // ACT: ejecutar la petición GET con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(get("/api/clientes/{id}", idBuscado)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y todos los campos del cliente devuelto
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@email.com"))
                .andExpect(jsonPath("$.telefono").value("+56912345678"))
                .andExpect(jsonPath("$.direccion").value("Av. Siempre Viva 742"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado con el ID correcto
        // --------------------------------------------------
        verify(clienteService, times(1)).obtenerPorId(idBuscado);

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el campo email llegara nulo en la respuesta, el test fallaría con:
         *
         *   Expected: $.email == "juan.perez@email.com"
         *   But was:  null
         *
         * QA debería reportar:
         *   - Endpoint: GET /api/clientes/1
         *   - Se esperaba: JSON con campo email = "juan.perez@email.com"
         *   - Se obtuvo:   JSON con campo email = null
         *   - Causa probable: el mapper en ClienteServiceImpl no está copiando
         *     el campo email del entity al ClienteResponseDTO al momento de construirlo
         *
         * Segundo escenario:
         *   Si el ID no existe y el service lanzara una excepción sin @ControllerAdvice,
         *   el test fallaría con:
         *
         *   Expected: 200
         *   But was:  500
         */
    }

    // =========================================================
    // TEST 4: PUT /api/clientes/{id} — actualizar()
    // =========================================================

    @Test
    @DisplayName("PUT /api/clientes/{id} - debería actualizar el cliente y retornar HTTP 200")
    void actualizar_cuandoDatosValidos_deberiaRetornar200ConClienteActualizado() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID, el body de actualización y la respuesta simulada
        // --------------------------------------------------
        Long idActualizar = 1L;

        ClienteRequestDTO requestActualizado = new ClienteRequestDTO();
        requestActualizado.setNombre("Juan Pérez Modificado");
        requestActualizado.setEmail("juan.perez.mod@email.com");
        requestActualizado.setTelefono("+56912345679");
        requestActualizado.setDireccion("Av. Siempre Viva 742, Depto 10");

        ClienteResponseDTO responseActualizado = ClienteResponseDTO.builder()
                .id(idActualizar)
                .nombre("Juan Pérez Modificado")
                .email("juan.perez.mod@email.com")
                .telefono("+56912345679")
                .direccion("Av. Siempre Viva 742, Depto 10")
                .build();

        // Cuando el service reciba cualquier ID y cualquier ClienteRequestDTO, devuelve la respuesta modificada
        // NOTA: Usamos any() porque Jackson deserializa en una instancia nueva y no tenemos @EqualsAndHashCode en el DTO
        when(clienteService.actualizarCliente(any(Long.class), any(ClienteRequestDTO.class)))
                .thenReturn(responseActualizado);

        // --------------------------------------------------
        // ACT: ejecutar la petición PUT con ID en URL y body JSON
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/clientes/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestActualizado)))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 200 y los campos actualizados
                // --------------------------------------------------
                .andExpect(status().isOk())                              // HTTP 200 OK
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Modificado"))
                .andExpect(jsonPath("$.email").value("juan.perez.mod@email.com"))
                .andExpect(jsonPath("$.telefono").value("+56912345679"))
                .andExpect(jsonPath("$.direccion").value("Av. Siempre Viva 742, Depto 10"));

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado con los parámetros correctos
        // --------------------------------------------------
        verify(clienteService, times(1)).actualizarCliente(any(Long.class), any(ClienteRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA para QA:
         * -------------------------------------------------------
         * Si el teléfono no se actualizara correctamente y el service devolviera
         * el valor anterior ("+56912345678"), el test fallaría con:
         *
         *   Expected: $.telefono == "+56912345679"
         *   But was:  "+56912345678"
         *
         * QA debería reportar:
         *   - Endpoint: PUT /api/clientes/1
         *   - Se esperaba: JSON con telefono = "+56912345679"
         *   - Se obtuvo:   JSON con telefono = "+56912345678"
         *   - Causa probable: el método actualizarCliente() en el service
         *     no está mapeando correctamente el campo telefono del request al entity
         */
    }

    // =========================================================
    // TEST 5: DELETE /api/clientes/{id} — eliminar()
    // =========================================================

    @Test
    @DisplayName("DELETE /api/clientes/{id} - debería eliminar el cliente y retornar HTTP 204")
    void eliminar_cuandoIdExiste_deberiaRetornar204SinContenido() throws Exception {

        // --------------------------------------------------
        // ARRANGE: preparar el ID del cliente a eliminar
        // --------------------------------------------------
        Long idEliminar = 1L;

        // eliminarCliente() es void → usamos doNothing() para simular éxito
        org.mockito.Mockito.doNothing().when(clienteService).eliminarCliente(idEliminar);

        // --------------------------------------------------
        // ACT: ejecutar la petición DELETE con el ID en la URL
        // --------------------------------------------------
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/clientes/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

                // --------------------------------------------------
                // ASSERT: verificar HTTP 204 y que el body esté vacío
                // --------------------------------------------------
                .andExpect(status().isNoContent())       // HTTP 204 No Content
                .andExpect(content().string(""));        // Body completamente vacío

        // --------------------------------------------------
        // VERIFY: comprobar que el service fue llamado exactamente 1 vez con el ID correcto
        // --------------------------------------------------
        verify(clienteService, times(1)).eliminarCliente(idEliminar);

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
         *   - Endpoint: DELETE /api/clientes/1
         *   - Se esperaba: HTTP 204 No Content (body vacío)
         *   - Se obtuvo:   HTTP 200 OK
         *   - Causa probable: el developer usó ResponseEntity.ok().build()
         *     en lugar de ResponseEntity.noContent().build()
         */
    }
}



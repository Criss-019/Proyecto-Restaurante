package com.restaurante.ms_reservas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurante.ms_reservas.dto.ReservaRequestDTO;
import com.restaurante.ms_reservas.dto.ReservaResponseDTO;
import com.restaurante.ms_reservas.service.ReservaService;
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
 * Pruebas unitarias para ReservaController.
 * 
 * Estrategia:
 * - ReservaController es REAL (instanciado por @WebMvcTest).
 * - ReservaService es SIMULADO con @MockBean.
 * - MockMvc simula peticiones HTTP.
 */
@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservaService reservaService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/reservas - Debe crear una reserva y retornar HTTP 201 Created")
    void crear_DebeRetornar201Created_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar datos de entrada y respuesta simulada
        LocalDateTime fechaFutura = LocalDateTime.now().plusDays(2);
        
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setClienteId(5L);
        request.setFechaHora(fechaFutura);
        request.setCantidadPersonas(4);
        request.setObservaciones("Mesa cerca de la ventana");

        ReservaResponseDTO responseSimulado = ReservaResponseDTO.builder()
                .id(1L)
                .clienteId(5L)
                .fechaHora(fechaFutura)
                .cantidadPersonas(4)
                .estado("PENDIENTE")
                .observaciones("Mesa cerca de la ventana")
                .build();

        when(reservaService.crearReserva(any(ReservaRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP POST
        mockMvc.perform(post("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar el estado HTTP 201 Created y los atributos devueltos en el JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(5))
                .andExpect(jsonPath("$.cantidadPersonas").value(4))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.observaciones").value("Mesa cerca de la ventana"));

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez
        verify(reservaService, times(1)).crearReserva(any(ReservaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 201 Created se obtiene HTTP 400 Bad Request por validación de fecha o cantidad:
         * - QA debe reportar: "Falla al crear reserva: La API responde 400 Bad Request al enviar ReservaRequestDTO válido."
         * - Desarrollo debe revisar: Las anotaciones @FutureOrPresent, @Min o @Max en ReservaRequestDTO.
         */
    }

    @Test
    @DisplayName("GET /api/reservas - Debe retornar lista de reservas con HTTP 200 OK")
    void listar_DebeRetornar200OK_YListaDeReservas() throws Exception {
        // ARRANGE: Preparar lista simulada de reservas
        LocalDateTime fecha1 = LocalDateTime.now().plusDays(1);
        LocalDateTime fecha2 = LocalDateTime.now().plusDays(3);

        ReservaResponseDTO reserva1 = ReservaResponseDTO.builder()
                .id(1L)
                .clienteId(5L)
                .fechaHora(fecha1)
                .cantidadPersonas(2)
                .estado("PENDIENTE")
                .observaciones("Sin gluten")
                .build();

        ReservaResponseDTO reserva2 = ReservaResponseDTO.builder()
                .id(2L)
                .clienteId(6L)
                .fechaHora(fecha2)
                .cantidadPersonas(8)
                .estado("CONFIRMADA")
                .observaciones(null)
                .build();

        when(reservaService.obtenerTodas()).thenReturn(java.util.Arrays.asList(reserva1, reserva2));

        // ACT: Ejecutar la petición HTTP GET
        mockMvc.perform(get("/api/reservas")
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y estructura del arreglo JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].cantidadPersonas").value(2))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].estado").value("CONFIRMADA"));

        // VERIFY: Comprobar invocación única al servicio
        verify(reservaService, times(1)).obtenerTodas();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla al listar reservas: El servidor respondió 500 Internal Server Error."
         * - Desarrollo debe revisar: Excepciones no controladas en ReservaService.obtenerTodas() o en el mapeo de entidades a DTO.
         */
    }

    @Test
    @DisplayName("GET /api/reservas/{id} - Debe retornar una reserva existente con HTTP 200 OK")
    void obtenerPorId_DebeRetornar200OK_CuandoReservaExiste() throws Exception {
        // ARRANGE: Preparar datos simulados para una reserva con ID 1L
        Long idBusqueda = 1L;
        LocalDateTime fechaReserva = LocalDateTime.now().plusDays(2);

        ReservaResponseDTO responseSimulado = ReservaResponseDTO.builder()
                .id(idBusqueda)
                .clienteId(5L)
                .fechaHora(fechaReserva)
                .cantidadPersonas(4)
                .estado("PENDIENTE")
                .observaciones("Requiere silla para bebé")
                .build();

        when(reservaService.obtenerPorId(idBusqueda)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP GET con el ID en la URL
        mockMvc.perform(get("/api/reservas/{id}", idBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y coincidencia de atributos en el JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(5))
                .andExpect(jsonPath("$.cantidadPersonas").value(4))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.observaciones").value("Requiere silla para bebé"));

        // VERIFY: Comprobar que el servicio fue llamado con el ID correcto
        verify(reservaService, times(1)).obtenerPorId(idBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 404 Not Found cuando la reserva existe:
         * - QA debe reportar: "Falla al obtener reserva por ID: La API devuelve 404 Not Found para un ID válido."
         * - Desarrollo debe revisar: La anotación @PathVariable Long id o la lógica en ReservaService.obtenerPorId().
         */
    }

    @Test
    @DisplayName("PUT /api/reservas/{id} - Debe actualizar una reserva y retornar HTTP 200 OK")
    void actualizar_DebeRetornar200OK_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar ID, solicitud con datos actualizados y respuesta simulada
        Long idActualizar = 1L;
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(5);

        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setClienteId(5L);
        request.setFechaHora(nuevaFecha);
        request.setCantidadPersonas(6);
        request.setObservaciones("Menú vegetariano requerido");

        ReservaResponseDTO responseSimulado = ReservaResponseDTO.builder()
                .id(idActualizar)
                .clienteId(5L)
                .fechaHora(nuevaFecha)
                .cantidadPersonas(6)
                .estado("PENDIENTE")
                .observaciones("Menú vegetariano requerido")
                .build();

        when(reservaService.actualizarReserva(eq(idActualizar), any(ReservaRequestDTO.class))).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PUT enviando el ID en la URL y el JSON en el cuerpo
        mockMvc.perform(put("/api/reservas/{id}", idActualizar)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar código HTTP 200 OK y coincidencia de datos actualizados
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cantidadPersonas").value(6))
                .andExpect(jsonPath("$.observaciones").value("Menú vegetariano requerido"));

        // VERIFY: Comprobar invocación única del servicio con los parámetros correctos
        verify(reservaService, times(1)).actualizarReserva(eq(idActualizar), any(ReservaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por validación:
         * - QA debe reportar: "Falla al actualizar reserva: La API devuelve 400 Bad Request enviando datos válidos."
         * - Desarrollo debe revisar: Las restricciones @FutureOrPresent, @Min y @Max en ReservaRequestDTO.
         */
    }

    @Test
    @DisplayName("PATCH /api/reservas/{id}/estado - Debe actualizar el estado de la reserva con HTTP 200 OK")
    void cambiarEstado_DebeRetornar200OK_CuandoParametrosSonValidos() throws Exception {
        // ARRANGE: Preparar ID y nuevo estado simulado
        Long idReserva = 1L;
        String nuevoEstado = "CONFIRMADA";

        ReservaResponseDTO responseSimulado = ReservaResponseDTO.builder()
                .id(idReserva)
                .clienteId(5L)
                .fechaHora(LocalDateTime.now().plusDays(2))
                .cantidadPersonas(4)
                .estado(nuevoEstado)
                .observaciones("Mesa cerca de la ventana")
                .build();

        when(reservaService.cambiarEstado(idReserva, nuevoEstado)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PATCH enviando el parámetro estado
        mockMvc.perform(patch("/api/reservas/{id}/estado", idReserva)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y que el estado devuelto sea el actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));

        // VERIFY: Comprobar invocación del servicio con los parámetros exactos
        verify(reservaService, times(1)).cambiarEstado(idReserva, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por falta del parámetro 'estado':
         * - QA debe reportar: "Falla al cambiar estado de reserva: La API devuelve 400 Bad Request sin el query param 'estado'."
         * - Desarrollo debe revisar: La anotación @RequestParam String estado en ReservaController.
         */
    }

    @Test
    @DisplayName("DELETE /api/reservas/{id} - Debe eliminar la reserva y retornar HTTP 204 No Content")
    void eliminar_DebeRetornar204NoContent_CuandoReservaExiste() throws Exception {
        // ARRANGE: Preparar ID de la reserva a eliminar
        Long idEliminar = 1L;

        // Simular que el servicio ejecuta la eliminación sin retornar valor (void)
        doNothing().when(reservaService).eliminarReserva(idEliminar);

        // ACT: Ejecutar la petición HTTP DELETE con el ID en la URL
        mockMvc.perform(delete("/api/reservas/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 204 No Content (sin cuerpo en la respuesta)
                .andExpect(status().isNoContent());

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez con el ID correcto
        verify(reservaService, times(1)).eliminarReserva(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 204 No Content se obtiene HTTP 404 Not Found:
         * - QA debe reportar: "Falla al eliminar reserva: La API responde 404 Not Found para un ID existente."
         * - Desarrollo debe revisar: La lógica en ReservaService.eliminarReserva() y si se está
         *   lanzando ResourceNotFoundException al no encontrar la entidad antes de eliminarla.
         */
    }
}

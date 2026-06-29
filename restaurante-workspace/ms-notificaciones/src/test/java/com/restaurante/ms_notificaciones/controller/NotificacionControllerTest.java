package com.restaurante.ms_notificaciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.restaurante.ms_notificaciones.dto.NotificacionRequestDTO;
import com.restaurante.ms_notificaciones.dto.NotificacionResponseDTO;
import com.restaurante.ms_notificaciones.service.NotificacionService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Pruebas unitarias para NotificacionController.
 * 
 * Estrategia:
 * - NotificacionController es REAL (instanciado por @WebMvcTest).
 * - NotificacionService es SIMULADO con @MockBean.
 * - MockMvc simula peticiones HTTP.
 */
@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificacionService notificacionService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    @DisplayName("POST /api/notificaciones - Debe crear y enviar una notificación correctamente")
    void enviar_DebeRetornar201Created_CuandoRequestEsValido() throws Exception {
        // ARRANGE: Preparar datos de entrada y respuesta simulada
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setClienteId(100L);
        request.setDestinatario("cliente@restaurante.com");
        request.setTipo("EMAIL");
        request.setAsunto("Confirmación de Reserva");
        request.setMensaje("Su reserva ha sido confirmada exitosamente.");

        NotificacionResponseDTO responseSimulado = NotificacionResponseDTO.builder()
                .id(1L)
                .clienteId(100L)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Confirmación de Reserva")
                .mensaje("Su reserva ha sido confirmada exitosamente.")
                .estado("ENVIADO")
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.crearYEnviarNotificacion(any(NotificacionRequestDTO.class)))
                .thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP POST
        mockMvc.perform(post("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

        // ASSERT: Verificar el estado HTTP 201 Created y los valores devueltos en la respuesta JSON
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(100))
                .andExpect(jsonPath("$.destinatario").value("cliente@restaurante.com"))
                .andExpect(jsonPath("$.tipo").value("EMAIL"))
                .andExpect(jsonPath("$.asunto").value("Confirmación de Reserva"))
                .andExpect(jsonPath("$.mensaje").value("Su reserva ha sido confirmada exitosamente."))
                .andExpect(jsonPath("$.estado").value("ENVIADO"));

        // VERIFY: Comprobar que el servicio fue invocado exactamente 1 vez
        verify(notificacionService, times(1)).crearYEnviarNotificacion(any(NotificacionRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 201 Created se obtiene HTTP 400 Bad Request:
         * - QA debe reportar: "Falla en creación de notificación: El servidor respondió 400 Bad Request al enviar NotificacionRequestDTO válido."
         * - Desarrollo debe revisar: Las anotaciones de validación (@NotNull, @NotBlank) en NotificacionRequestDTO o la deserialización JSON en el Controller.
         */
    }

    @Test
    @DisplayName("GET /api/notificaciones - Debe retornar lista de notificaciones con HTTP 200 OK")
    void listar_DebeRetornar200OK_YListaDeNotificaciones() throws Exception {
        // ARRANGE: Preparar lista simulada de notificaciones
        NotificacionResponseDTO notificacion1 = NotificacionResponseDTO.builder()
                .id(1L)
                .clienteId(100L)
                .destinatario("cliente1@restaurante.com")
                .tipo("EMAIL")
                .asunto("Reserva Confirmada")
                .mensaje("Mensaje 1")
                .estado("ENVIADO")
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(LocalDateTime.now())
                .build();

        NotificacionResponseDTO notificacion2 = NotificacionResponseDTO.builder()
                .id(2L)
                .clienteId(101L)
                .destinatario("+56912345678")
                .tipo("SMS")
                .asunto(null)
                .mensaje("Mensaje 2")
                .estado("PENDIENTE")
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(null)
                .build();

        when(notificacionService.obtenerTodas()).thenReturn(java.util.Arrays.asList(notificacion1, notificacion2));

        // ACT: Ejecutar la petición HTTP GET
        mockMvc.perform(get("/api/notificaciones")
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y estructura del array JSON
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].destinatario").value("cliente1@restaurante.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].tipo").value("SMS"));

        // VERIFY: Comprobar llamada al servicio
        verify(notificacionService, times(1)).obtenerTodas();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla al listar notificaciones: El servidor respondió 500 Internal Server Error."
         * - Desarrollo debe revisar: Excepciones no capturadas en NotificacionService.obtenerTodas() o fallos en el mapeo de entidades a DTO.
         */
    }

    @Test
    @DisplayName("GET /api/notificaciones/{id} - Debe retornar una notificación existente con HTTP 200 OK")
    void obtenerPorId_DebeRetornar200OK_CuandoNotificacionExiste() throws Exception {
        // ARRANGE: Preparar datos simulados para ID 1L
        Long idBusqueda = 1L;
        NotificacionResponseDTO responseSimulado = NotificacionResponseDTO.builder()
                .id(idBusqueda)
                .clienteId(100L)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Detalle de Reserva")
                .mensaje("Mensaje de prueba")
                .estado("ENVIADO")
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.obtenerPorId(idBusqueda)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP GET con el ID en el path
        mockMvc.perform(get("/api/notificaciones/{id}", idBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y que coincidan los campos devueltos
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteId").value(100))
                .andExpect(jsonPath("$.destinatario").value("cliente@restaurante.com"))
                .andExpect(jsonPath("$.tipo").value("EMAIL"))
                .andExpect(jsonPath("$.estado").value("ENVIADO"));

        // VERIFY: Comprobar que el servicio fue llamado con el ID correcto
        verify(notificacionService, times(1)).obtenerPorId(idBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 404 Not Found cuando la notificación existe:
         * - QA debe reportar: "Falla al obtener notificación por ID: La API responde 404 Not Found para un ID válido."
         * - Desarrollo debe revisar: La anotación @PathVariable o la lógica de búsqueda en NotificacionService.obtenerPorId().
         */
    }

    @Test
    @DisplayName("GET /api/notificaciones/cliente/{clienteId} - Debe retornar notificaciones asociadas al cliente con HTTP 200 OK")
    void obtenerPorClienteId_DebeRetornar200OK_CuandoClienteTieneNotificaciones() throws Exception {
        // ARRANGE: Preparar lista simulada de notificaciones para clienteId 100L
        Long clienteIdBusqueda = 100L;
        NotificacionResponseDTO notificacion1 = NotificacionResponseDTO.builder()
                .id(1L)
                .clienteId(clienteIdBusqueda)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Confirmación de Pedido")
                .mensaje("Su pedido está en preparación")
                .estado("ENVIADO")
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.obtenerPorClienteId(clienteIdBusqueda))
                .thenReturn(java.util.Collections.singletonList(notificacion1));

        // ACT: Ejecutar la petición HTTP GET con el clienteId en la URL
        mockMvc.perform(get("/api/notificaciones/cliente/{clienteId}", clienteIdBusqueda)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar respuesta 200 OK y la estructura de la lista devuelta
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clienteId").value(100))
                .andExpect(jsonPath("$[0].destinatario").value("cliente@restaurante.com"));

        // VERIFY: Comprobar que el servicio fue invocado con el clienteId correcto
        verify(notificacionService, times(1)).obtenerPorClienteId(clienteIdBusqueda);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene una lista vacía cuando el cliente sí tiene notificaciones:
         * - QA debe reportar: "Falla al obtener notificaciones por clienteId: La respuesta retorna un arreglo vacío para un cliente con registros."
         * - Desarrollo debe revisar: La consulta JPA o el filtro por clienteId en NotificacionService.obtenerPorClienteId().
         */
    }

    @Test
    @DisplayName("PATCH /api/notificaciones/{id}/estado - Debe actualizar el estado de la notificación con HTTP 200 OK")
    void cambiarEstado_DebeRetornar200OK_CuandoParametrosSonValidos() throws Exception {
        // ARRANGE: Preparar datos y respuesta simulada con nuevo estado
        Long idNotificacion = 1L;
        String nuevoEstado = "LEIDO";

        NotificacionResponseDTO responseSimulado = NotificacionResponseDTO.builder()
                .id(idNotificacion)
                .clienteId(100L)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Confirmación de Pedido")
                .mensaje("Mensaje de prueba")
                .estado(nuevoEstado)
                .fechaCreacion(LocalDateTime.now())
                .fechaEnvio(LocalDateTime.now())
                .build();

        when(notificacionService.cambiarEstado(idNotificacion, nuevoEstado)).thenReturn(responseSimulado);

        // ACT: Ejecutar la petición HTTP PATCH enviando el estado como RequestParam
        mockMvc.perform(patch("/api/notificaciones/{id}/estado", idNotificacion)
                        .param("estado", nuevoEstado)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar código HTTP 200 OK y que el estado devuelto sea el actualizado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("LEIDO"));

        // VERIFY: Comprobar invocación del servicio con parámetros correctos
        verify(notificacionService, times(1)).cambiarEstado(idNotificacion, nuevoEstado);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 200 OK se obtiene HTTP 400 Bad Request por falta del parámetro 'estado':
         * - QA debe reportar: "Falla en actualización de estado: La API devuelve 400 Bad Request al no recibir el query param 'estado'."
         * - Desarrollo debe revisar: La anotación @RequestParam String estado en el Controller.
         */
    }

    @Test
    @DisplayName("DELETE /api/notificaciones/{id} - Debe eliminar la notificación y retornar HTTP 204 No Content")
    void eliminar_DebeRetornar204NoContent_CuandoNotificacionExiste() throws Exception {
        // ARRANGE: Preparar ID a eliminar y configurar comportamiento del servicio (void)
        Long idEliminar = 1L;
        doNothing().when(notificacionService).eliminarNotificacion(idEliminar);

        // ACT: Ejecutar la petición HTTP DELETE
        mockMvc.perform(delete("/api/notificaciones/{id}", idEliminar)
                        .contentType(MediaType.APPLICATION_JSON))

        // ASSERT: Verificar que la respuesta sea HTTP 204 No Content
                .andExpect(status().isNoContent());

        // VERIFY: Comprobar que el servicio fue invocado con el ID a eliminar
        verify(notificacionService, times(1)).eliminarNotificacion(idEliminar);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 204 No Content se obtiene HTTP 200 OK o HTTP 500 Internal Server Error:
         * - QA debe reportar: "Falla en eliminación de notificación: Se esperaba HTTP 204 No Content pero la API respondió con otro código."
         * - Desarrollo debe revisar: El retorno del método eliminar en NotificacionController (ResponseEntity.noContent().build()).
         */
    }
}

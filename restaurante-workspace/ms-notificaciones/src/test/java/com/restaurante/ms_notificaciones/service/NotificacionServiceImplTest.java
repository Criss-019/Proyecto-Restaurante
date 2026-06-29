package com.restaurante.ms_notificaciones.service;

import com.restaurante.ms_notificaciones.dto.NotificacionRequestDTO;
import com.restaurante.ms_notificaciones.dto.NotificacionResponseDTO;
import com.restaurante.ms_notificaciones.entity.Notificacion;
import com.restaurante.ms_notificaciones.exception.ResourceNotFoundException;
import com.restaurante.ms_notificaciones.repository.NotificacionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para NotificacionServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (NotificacionRepository) con @Mock.
 * - Se inyectan los mocks en NotificacionServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 *
 * Nota sobre crearYEnviarNotificacion():
 * El método tiene dos fases internas de persistencia:
 *   1. Primer save() → guarda con estado PENDIENTE.
 *   2. Segundo save() → actualiza a ENVIADO tras la simulación de envío.
 * Por ello se configura un comportamiento escalonado con thenReturn(...).
 */
@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    // =========================================================================
    // crearYEnviarNotificacion()
    // =========================================================================

    @Test
    @DisplayName("crearYEnviarNotificacion - Debe registrar y simular el envío, retornando el DTO con estado ENVIADO")
    void crearYEnviarNotificacion_DebeRetornarNotificacionConEstadoEnviado_CuandoElEnvioEsExitoso() {
        // ARRANGE: Request de prueba para una notificación por email
        NotificacionRequestDTO request = new NotificacionRequestDTO();
        request.setClienteId(1L);
        request.setDestinatario("cliente@restaurante.com");
        request.setTipo("EMAIL");
        request.setAsunto("Tu pedido está listo");
        request.setMensaje("Hola, tu pedido #10 ha sido preparado y está en camino.");

        LocalDateTime ahora = LocalDateTime.now();

        // Primer save(): registro inicial con estado PENDIENTE
        Notificacion notificacionPendiente = Notificacion.builder()
                .id(1L).clienteId(1L)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Tu pedido está listo")
                .mensaje("Hola, tu pedido #10 ha sido preparado y está en camino.")
                .estado("PENDIENTE")
                .fechaCreacion(ahora)
                .fechaEnvio(null)
                .build();

        // Segundo save(): misma notificación tras la simulación de envío → ENVIADO
        Notificacion notificacionEnviada = Notificacion.builder()
                .id(1L).clienteId(1L)
                .destinatario("cliente@restaurante.com")
                .tipo("EMAIL")
                .asunto("Tu pedido está listo")
                .mensaje("Hola, tu pedido #10 ha sido preparado y está en camino.")
                .estado("ENVIADO")
                .fechaCreacion(ahora)
                .fechaEnvio(ahora)
                .build();

        // Comportamiento escalonado: primer save() → PENDIENTE, segundo save() → ENVIADO
        when(notificacionRepository.save(any(Notificacion.class)))
                .thenReturn(notificacionPendiente)
                .thenReturn(notificacionEnviada);

        // ACT: Invocar el método del servicio
        NotificacionResponseDTO resultado = notificacionService.crearYEnviarNotificacion(request);

        // ASSERT: El DTO final debe reflejar el estado ENVIADO tras la simulación
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID no coincide");
        assertEquals(1L, resultado.getClienteId(), "El clienteId no coincide");
        assertEquals("cliente@restaurante.com", resultado.getDestinatario());
        assertEquals("EMAIL", resultado.getTipo());
        assertEquals("Tu pedido está listo", resultado.getAsunto());
        assertEquals("ENVIADO", resultado.getEstado(),
                "El estado final debe ser ENVIADO tras la simulación de envío exitoso");
        assertNotNull(resultado.getFechaCreacion(), "La fecha de creación no debe ser nula");
        assertNotNull(resultado.getFechaEnvio(), "La fecha de envío no debe ser nula cuando el estado es ENVIADO");

        // VERIFY: save() debe haberse invocado exactamente 2 veces (fase 1 + fase 2)
        verify(notificacionRepository, times(2)).save(any(Notificacion.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la notificación queda en estado PENDIENTE y nunca transita a ENVIADO:
         * - QA debe reportar: "Falla en envío: La notificación se registra pero permanece en estado PENDIENTE."
         * - Desarrollo debe revisar: Si el bloque try del método está ejecutando correctamente el segundo save()
         *   con el estado ENVIADO y la fechaEnvio asignada.
         */
    }

    // =========================================================================
    // obtenerTodas()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodas - Debe retornar la lista completa del historial de notificaciones mapeadas a DTO")
    void obtenerTodas_DebeRetornarListaDeNotificacionResponseDTO() {
        // ARRANGE: Dos notificaciones con tipos y estados distintos
        LocalDateTime ahora = LocalDateTime.now();

        Notificacion n1 = Notificacion.builder()
                .id(1L).clienteId(1L)
                .destinatario("cliente1@restaurante.com").tipo("EMAIL")
                .asunto("Pedido listo").mensaje("Tu pedido #10 está en camino.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        Notificacion n2 = Notificacion.builder()
                .id(2L).clienteId(2L)
                .destinatario("+5491112345678").tipo("SMS")
                .asunto(null).mensaje("Tu pedido #11 fue confirmado.")
                .estado("FALLIDO").fechaCreacion(ahora).fechaEnvio(null)
                .build();

        when(notificacionRepository.findAll()).thenReturn(java.util.Arrays.asList(n1, n2));

        // ACT: Invocar el método
        java.util.List<NotificacionResponseDTO> resultado = notificacionService.obtenerTodas();

        // ASSERT: Verificar tamaño de lista y mapeo de campos clave en ambas notificaciones
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El historial debe contener 2 notificaciones");

        // Notificación 1 — EMAIL exitoso
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("EMAIL", resultado.get(0).getTipo());
        assertEquals("ENVIADO", resultado.get(0).getEstado());
        assertNotNull(resultado.get(0).getFechaEnvio(), "El EMAIL enviado debe tener fecha de envío");

        // Notificación 2 — SMS fallido (fechaEnvio nula)
        assertEquals(2L, resultado.get(1).getId());
        assertEquals("SMS", resultado.get(1).getTipo());
        assertEquals("FALLIDO", resultado.get(1).getEstado());
        assertNull(resultado.get(1).getFechaEnvio(), "Un SMS fallido no debe tener fecha de envío registrada");

        // VERIFY: findAll() invocado exactamente una vez
        verify(notificacionRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el historial retorna notificaciones sin el campo fechaEnvio cuando el estado es ENVIADO:
         * - QA debe reportar: "Falla en historial: Las notificaciones ENVIADAS no registran su fecha de envío."
         * - Desarrollo debe revisar: Si la fase 2 de crearYEnviarNotificacion() está asignando
         *   la fechaEnvio antes del segundo save().
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar la notificación mapeada a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarNotificacionResponseDTO_CuandoIdExiste() {
        // ARRANGE: Notificación simulada en base de datos
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Notificacion notificacion = Notificacion.builder()
                .id(id).clienteId(1L)
                .destinatario("cliente@restaurante.com").tipo("EMAIL")
                .asunto("Tu pedido está listo").mensaje("Pedido #10 en camino.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacion));

        // ACT: Invocar el método
        NotificacionResponseDTO resultado = notificacionService.obtenerPorId(id);

        // ASSERT: Verificar que todos los campos están correctamente mapeados
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId(), "El ID no coincide");
        assertEquals(1L, resultado.getClienteId(), "El clienteId no coincide");
        assertEquals("cliente@restaurante.com", resultado.getDestinatario());
        assertEquals("EMAIL", resultado.getTipo());
        assertEquals("Tu pedido está listo", resultado.getAsunto());
        assertEquals("Pedido #10 en camino.", resultado.getMensaje());
        assertEquals("ENVIADO", resultado.getEstado());
        assertNotNull(resultado.getFechaCreacion(), "La fecha de creación no debe ser nula");
        assertNotNull(resultado.getFechaEnvio(), "La fecha de envío no debe ser nula para una notificación ENVIADA");

        // VERIFY: findById invocado 1 vez con el ID correcto
        verify(notificacionRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular notificación inexistente
        Long idInvalido = 999L;
        when(notificacionRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificacionService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando la notificación no existe"
        );

        assertEquals("Notificación no encontrada con ID: 999", exception.getMessage(),
                "El mensaje de excepción debe indicar el ID buscado");

        // VERIFY: findById invocado 1 vez, sin más interacciones
        verify(notificacionRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con HTTP 500 en lugar de 404 al consultar una notificación inexistente:
         * - QA debe reportar: "Falla al buscar notificación: La API responde 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el @ExceptionHandler para ResourceNotFoundException está configurado
         *   en el GlobalExceptionHandler del módulo ms-notificaciones.
         */
    }

    // =========================================================================
    // obtenerPorClienteId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorClienteId - Debe retornar el historial de notificaciones de un cliente específico")
    void obtenerPorClienteId_DebeRetornarListaDeNotificaciones_CuandoClienteIdExiste() {
        // ARRANGE: Dos notificaciones del mismo cliente con canales distintos
        Long clienteId = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Notificacion n1 = Notificacion.builder()
                .id(1L).clienteId(clienteId)
                .destinatario("cliente@restaurante.com").tipo("EMAIL")
                .asunto("Pedido confirmado").mensaje("Tu pedido #10 fue recibido.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        Notificacion n2 = Notificacion.builder()
                .id(2L).clienteId(clienteId)
                .destinatario("+5491112345678").tipo("SMS")
                .asunto(null).mensaje("Tu pedido #10 está en camino.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        when(notificacionRepository.findByClienteId(clienteId))
                .thenReturn(java.util.Arrays.asList(n1, n2));

        // ACT: Invocar el método
        java.util.List<NotificacionResponseDTO> resultado = notificacionService.obtenerPorClienteId(clienteId);

        // ASSERT: Verificar que ambas notificaciones del cliente fueron retornadas
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El cliente debe tener 2 notificaciones en su historial");

        // Verificar que todas pertenecen al mismo cliente
        resultado.forEach(dto ->
                assertEquals(clienteId, dto.getClienteId(),
                        "Todas las notificaciones deben pertenecer al clienteId: " + clienteId)
        );

        // Verificar mapeo individual
        assertEquals("EMAIL", resultado.get(0).getTipo());
        assertEquals("Pedido confirmado", resultado.get(0).getAsunto());
        assertEquals("SMS", resultado.get(1).getTipo());
        assertNull(resultado.get(1).getAsunto(), "El SMS no debe tener asunto");

        // VERIFY: findByClienteId invocado 1 vez
        verify(notificacionRepository, times(1)).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("obtenerPorClienteId - Debe retornar lista vacía cuando el cliente no tiene notificaciones")
    void obtenerPorClienteId_DebeRetornarListaVacia_CuandoClienteNoTieneNotificaciones() {
        // ARRANGE: Cliente sin historial de notificaciones
        Long clienteIdSinHistorial = 99L;
        when(notificacionRepository.findByClienteId(clienteIdSinHistorial))
                .thenReturn(java.util.Collections.emptyList());

        // ACT: Invocar el método
        java.util.List<NotificacionResponseDTO> resultado = notificacionService.obtenerPorClienteId(clienteIdSinHistorial);

        // ASSERT: La lista debe estar vacía pero nunca ser nula
        assertNotNull(resultado, "La lista no debe ser nula aunque no haya notificaciones");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía para un cliente sin notificaciones");

        // VERIFY: findByClienteId invocado 1 vez
        verify(notificacionRepository, times(1)).findByClienteId(clienteIdSinHistorial);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint GET /notificaciones/cliente/{id} retorna HTTP 404 en lugar de lista vacía
         * cuando el cliente no tiene notificaciones:
         * - QA debe reportar: "Falla en historial por cliente: La API responde 404 en lugar de lista vacía []."
         * - Desarrollo debe revisar: Si el método retorna directamente el stream vacío sin lanzar excepción,
         *   ya que obtenerPorClienteId() no usa orElseThrow() sino findByClienteId() que devuelve List.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe actualizar el estado de la notificación y retornar el DTO actualizado")
    void cambiarEstado_DebeActualizarEstado_CuandoIdExiste() {
        // ARRANGE: Notificación existente con estado ENVIADO
        Long id = 1L;
        String nuevoEstado = "REBOTADO";
        LocalDateTime ahora = LocalDateTime.now();

        Notificacion notificacionExistente = Notificacion.builder()
                .id(id).clienteId(1L)
                .destinatario("cliente@restaurante.com").tipo("EMAIL")
                .asunto("Tu pedido está listo").mensaje("Pedido #10 en camino.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        // Notificación que el repositorio retorna tras el save() con nuevo estado
        Notificacion notificacionActualizada = Notificacion.builder()
                .id(id).clienteId(1L)
                .destinatario("cliente@restaurante.com").tipo("EMAIL")
                .asunto("Tu pedido está listo").mensaje("Pedido #10 en camino.")
                .estado(nuevoEstado).fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacionExistente));
        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(notificacionActualizada);

        // ACT: Cambiar el estado
        NotificacionResponseDTO resultado = notificacionService.cambiarEstado(id, nuevoEstado);

        // ASSERT: Verificar que el estado cambió y el resto de campos permanecen intactos
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId());
        assertEquals(nuevoEstado, resultado.getEstado(), "El estado debe haber cambiado a REBOTADO");
        assertEquals("cliente@restaurante.com", resultado.getDestinatario());
        assertEquals("EMAIL", resultado.getTipo());

        // VERIFY: findById y save invocados una vez cada uno
        verify(notificacionRepository, times(1)).findById(id);
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular notificación inexistente
        Long idInvalido = 999L;
        when(notificacionRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificacionService.cambiarEstado(idInvalido, "ANULADO"),
                "Debe lanzar ResourceNotFoundException cuando la notificación no existe"
        );

        assertEquals("Notificación no encontrada con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, save NO debe haberse invocado
        verify(notificacionRepository, times(1)).findById(idInvalido);
        verify(notificacionRepository, never()).save(any(Notificacion.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar actualizar el estado de una notificación inexistente la API devuelve HTTP 200:
         * - QA debe reportar: "Falla al actualizar estado: La API responde exitosamente para una notificación que no existe."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en cambiarEstado() antes de llamar a setEstado() y save().
         */
    }

    // =========================================================================
    // eliminarNotificacion()
    // =========================================================================

    @Test
    @DisplayName("eliminarNotificacion - Debe eliminar la notificación correctamente cuando el ID existe")
    void eliminarNotificacion_DebeEliminarNotificacion_CuandoIdExiste() {
        // ARRANGE: Notificación existente lista para ser eliminada
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Notificacion notificacion = Notificacion.builder()
                .id(id).clienteId(1L)
                .destinatario("cliente@restaurante.com").tipo("EMAIL")
                .asunto("Tu pedido está listo").mensaje("Pedido #10 en camino.")
                .estado("ENVIADO").fechaCreacion(ahora).fechaEnvio(ahora)
                .build();

        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacion));
        doNothing().when(notificacionRepository).delete(notificacion);

        // ACT & ASSERT: Eliminar la notificación y verificar que no lance excepción
        assertDoesNotThrow(() -> notificacionService.eliminarNotificacion(id),
                "No debe lanzar excepción al eliminar una notificación existente");

        // VERIFY: findById y delete invocados una vez cada uno
        verify(notificacionRepository, times(1)).findById(id);
        verify(notificacionRepository, times(1)).delete(notificacion);
    }

    @Test
    @DisplayName("eliminarNotificacion - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarNotificacion_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular notificación inexistente
        Long idInvalido = 999L;
        when(notificacionRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> notificacionService.eliminarNotificacion(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando la notificación a eliminar no existe"
        );

        assertEquals("Notificación no encontrada con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, delete NO debe haberse invocado
        verify(notificacionRepository, times(1)).findById(idInvalido);
        verify(notificacionRepository, never()).delete(any(Notificacion.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /notificaciones/{id} responde HTTP 204 (sin contenido) para una notificación inexistente:
         * - QA debe reportar: "Falla al eliminar notificación: La API confirma eliminación de un registro inexistente."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está presente antes del delete(),
         *   y si el GlobalExceptionHandler está capturando y convirtiendo la excepción a HTTP 404.
         */
    }
}




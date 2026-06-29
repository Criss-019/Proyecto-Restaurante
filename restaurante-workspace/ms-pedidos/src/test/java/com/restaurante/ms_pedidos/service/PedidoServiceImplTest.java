package com.restaurante.ms_pedidos.service;

import com.restaurante.ms_pedidos.client.ClienteClient;
import com.restaurante.ms_pedidos.client.NotificacionClient;
import com.restaurante.ms_pedidos.client.dto.ClienteDTO;
import com.restaurante.ms_pedidos.client.dto.NotificacionRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoRequestDTO;
import com.restaurante.ms_pedidos.dto.PedidoResponseDTO;
import com.restaurante.ms_pedidos.entity.Pedido;
import com.restaurante.ms_pedidos.exception.ResourceNotFoundException;
import com.restaurante.ms_pedidos.repository.PedidoRepository;
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
 * Pruebas unitarias para PedidoServiceImpl.
 *
 * Estrategia:
 * - MockitoExtension para pruebas unitarias puras y rápidas.
 * - Simular la base de datos (PedidoRepository) y clientes Feign (ClienteClient, NotificacionClient).
 * - Estructura: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class PedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ClienteClient clienteClient;

    @Mock
    private NotificacionClient notificacionClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    // =========================================================================
    // crearPedido()
    // =========================================================================

    @Test
    @DisplayName("crearPedido - Debe guardar el pedido y enviar notificación si el cliente existe")
    void crearPedido_DebeRetornarPedidoResponseDTO_CuandoClienteExisteYNotificacionEsExitosa() {
        // ARRANGE
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(1L);
        request.setTipoEntrega("DOMICILIO");

        ClienteDTO clienteMock = new ClienteDTO();
        clienteMock.setId(1L);
        clienteMock.setNombre("Juan Pérez");
        clienteMock.setEmail("juan@correo.com");

        Pedido pedidoGuardado = Pedido.builder()
                .id(100L)
                .clienteId(1L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("DOMICILIO")
                .estado("CREADO")
                .total(0.0)
                .build();

        when(clienteClient.obtenerPorId(1L)).thenReturn(clienteMock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);
        doNothing().when(notificacionClient).enviarNotificacion(any(NotificacionRequestDTO.class));

        // ACT
        PedidoResponseDTO resultado = pedidoService.crearPedido(request);

        // ASSERT
        assertNotNull(resultado, "El pedido creado no debe ser nulo");
        assertEquals(100L, resultado.getId());
        assertEquals(1L, resultado.getClienteId());
        assertEquals("DOMICILIO", resultado.getTipoEntrega());
        assertEquals("CREADO", resultado.getEstado());
        assertEquals(0.0, resultado.getTotal());
        assertNotNull(resultado.getFechaPedido());

        // VERIFY
        verify(clienteClient, times(1)).obtenerPorId(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el pedido se guarda localmente pero la verificación del cliente nunca se ejecuta:
         * - QA debe reportar: "Falla de integridad: Es posible registrar pedidos para IDs de clientes inexistentes."
         * - Desarrollo debe revisar: Si el llamado a clienteClient.obtenerPorId está siendo saltado o ejecutado en segundo plano de forma incorrecta.
         */
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar ResourceNotFoundException si el cliente no existe (NotFound)")
    void crearPedido_DebeLanzarResourceNotFoundException_CuandoClienteNoExiste() {
        // ARRANGE
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(999L);
        request.setTipoEntrega("LLEVAR");

        // Simular FeignException.NotFound
        feign.FeignException.NotFound mockNotFound = mock(feign.FeignException.NotFound.class);
        when(clienteClient.obtenerPorId(999L)).thenThrow(mockNotFound);

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pedidoService.crearPedido(request),
                "Debe lanzar ResourceNotFoundException cuando el cliente no existe en ms-clientes"
        );

        assertTrue(exception.getMessage().contains("El cliente con ID 999 no existe."));

        // VERIFY: No se guarda el pedido ni se envía notificación
        verify(clienteClient, times(1)).obtenerPorId(999L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(notificacionClient);
    }

    @Test
    @DisplayName("crearPedido - Debe lanzar RuntimeException si ms-clientes responde con error interno genérico de Feign")
    void crearPedido_DebeLanzarRuntimeException_CuandoMsClientesFallaGenericamente() {
        // ARRANGE
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(1L);
        request.setTipoEntrega("MESA");

        // Simular FeignException genérico
        feign.FeignException mockException = mock(feign.FeignException.class);
        when(clienteClient.obtenerPorId(1L)).thenThrow(mockException);

        // ACT & ASSERT
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> pedidoService.crearPedido(request),
                "Debe lanzar RuntimeException cuando ocurre un error de Feign diferente a NotFound"
        );

        assertEquals("Error interno al verificar el cliente.", exception.getMessage());

        // VERIFY: No se guarda el pedido ni se notifica
        verify(clienteClient, times(1)).obtenerPorId(1L);
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verifyNoInteractions(notificacionClient);
    }

    @Test
    @DisplayName("crearPedido - Debe guardar el pedido con éxito aun cuando la notificación por Feign falle (Resiliencia)")
    void crearPedido_DebeRetornarPedidoResponseDTO_CuandoNotificacionFalla() {
        // ARRANGE
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(1L);
        request.setTipoEntrega("DOMICILIO");

        ClienteDTO clienteMock = new ClienteDTO();
        clienteMock.setId(1L);
        clienteMock.setNombre("Juan Pérez");
        clienteMock.setEmail("juan@correo.com");

        Pedido pedidoGuardado = Pedido.builder()
                .id(100L)
                .clienteId(1L)
                .fechaPedido(LocalDateTime.now())
                .tipoEntrega("DOMICILIO")
                .estado("CREADO")
                .total(0.0)
                .build();

        when(clienteClient.obtenerPorId(1L)).thenReturn(clienteMock);
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        // Simular fallo de Feign al enviar la notificación
        feign.FeignException mockException = mock(feign.FeignException.class);
        doThrow(mockException).when(notificacionClient).enviarNotificacion(any(NotificacionRequestDTO.class));

        // ACT
        PedidoResponseDTO resultado = pedidoService.crearPedido(request);

        // ASSERT: El pedido debe haberse creado de todas formas
        assertNotNull(resultado, "El pedido creado debe ser retornado a pesar de la falla de notificación");
        assertEquals(100L, resultado.getId());

        // VERIFY: Todo se llamó, pero el fallo de notificación fue capturado y tolerado
        verify(clienteClient, times(1)).obtenerPorId(1L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(notificacionClient, times(1)).enviarNotificacion(any(NotificacionRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el sistema completo cancela la creación del pedido (hace rollback o aborta) porque el servicio de notificaciones está caído:
         * - QA debe reportar: "Falla de resiliencia: Si el servicio de notificaciones está caído, no se pueden registrar nuevos pedidos."
         * - Desarrollo debe revisar: Si el bloque try/catch en el envío de notificación de crearPedido está atrapando correctamente la excepción FeignException.
         */
    }

    // =========================================================================
    // obtenerTodos()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodos - Debe retornar una lista de todos los pedidos mapeados a DTO")
    void obtenerTodos_DebeRetornarListaDePedidoResponseDTO() {
        // ARRANGE
        LocalDateTime ahora = LocalDateTime.now();
        Pedido p1 = Pedido.builder()
                .id(1L).clienteId(10L).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(120.0).build();

        Pedido p2 = Pedido.builder()
                .id(2L).clienteId(11L).fechaPedido(ahora).tipoEntrega("LLEVAR")
                .estado("PAGADO").total(450.0).build();

        when(pedidoRepository.findAll()).thenReturn(java.util.Arrays.asList(p1, p2));

        // ACT
        java.util.List<PedidoResponseDTO> resultado = pedidoService.obtenerTodos();

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size());

        assertEquals(1L, resultado.get(0).getId());
        assertEquals("DOMICILIO", resultado.get(0).getTipoEntrega());
        assertEquals("CREADO", resultado.get(0).getEstado());

        assertEquals(2L, resultado.get(1).getId());
        assertEquals("LLEVAR", resultado.get(1).getTipoEntrega());
        assertEquals("PAGADO", resultado.get(1).getEstado());

        // VERIFY
        verify(pedidoRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el listado general de pedidos arroja una lista vacía cuando existen pedidos en base de datos:
         * - QA debe reportar: "Falla al listar pedidos: El endpoint GET /pedidos no devuelve registros."
         * - Desarrollo debe revisar: Si la llamada al repositorio findAll() o el mapeo stream/mapToDTO están retornando una lista sin elementos.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el pedido mapeado a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarPedidoResponseDTO_CuandoIdExiste() {
        // ARRANGE
        Long idBusqueda = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .id(idBusqueda).clienteId(10L).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(120.0).build();

        when(pedidoRepository.findById(idBusqueda)).thenReturn(Optional.of(pedido));

        // ACT
        PedidoResponseDTO resultado = pedidoService.obtenerPorId(idBusqueda);

        // ASSERT
        assertNotNull(resultado, "El pedido retornado no debe ser nulo");
        assertEquals(idBusqueda, resultado.getId());
        assertEquals(10L, resultado.getClienteId());
        assertEquals("DOMICILIO", resultado.getTipoEntrega());
        assertEquals("CREADO", resultado.getEstado());
        assertEquals(120.0, resultado.getTotal());

        // VERIFY
        verify(pedidoRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pedidoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pedidoService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException si el pedido no existe"
        );

        assertEquals("Pedido no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pedidoRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar un pedido por ID inexistente la API devuelve HTTP 500 en lugar de 404:
         * - QA debe reportar: "Falla al buscar pedido inexistente: Responde con 500 en lugar de 404."
         * - Desarrollo debe revisar: Si el ExceptionHandler del módulo ms-pedidos intercepta adecuadamente ResourceNotFoundException.
         */
    }

    // =========================================================================
    // obtenerPorClienteId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorClienteId - Debe retornar el historial de pedidos de un cliente")
    void obtenerPorClienteId_DebeRetornarListaDePedidos_CuandoClienteTienePedidos() {
        // ARRANGE
        Long clienteId = 10L;
        LocalDateTime ahora = LocalDateTime.now();
        Pedido p1 = Pedido.builder()
                .id(1L).clienteId(clienteId).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(120.0).build();

        Pedido p2 = Pedido.builder()
                .id(2L).clienteId(clienteId).fechaPedido(ahora).tipoEntrega("LLEVAR")
                .estado("PAGADO").total(450.0).build();

        when(pedidoRepository.findByClienteId(clienteId)).thenReturn(java.util.Arrays.asList(p1, p2));

        // ACT
        java.util.List<PedidoResponseDTO> resultado = pedidoService.obtenerPorClienteId(clienteId);

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size());
        assertEquals(clienteId, resultado.get(0).getClienteId());
        assertEquals(clienteId, resultado.get(1).getClienteId());

        // VERIFY
        verify(pedidoRepository, times(1)).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("obtenerPorClienteId - Debe retornar una lista vacía cuando el cliente no tiene pedidos")
    void obtenerPorClienteId_DebeRetornarListaVacia_CuandoClienteNoTienePedidos() {
        // ARRANGE
        Long clienteIdInvalido = 999L;
        when(pedidoRepository.findByClienteId(clienteIdInvalido)).thenReturn(java.util.Collections.emptyList());

        // ACT
        java.util.List<PedidoResponseDTO> resultado = pedidoService.obtenerPorClienteId(clienteIdInvalido);

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía para un cliente sin pedidos");

        // VERIFY
        verify(pedidoRepository, times(1)).findByClienteId(clienteIdInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API devuelve un error HTTP 404 en lugar de una lista vacía [] al buscar el historial de un cliente sin pedidos:
         * - QA debe reportar: "Falla de consulta: La API devuelve 404 al buscar pedidos de un cliente que no ha comprado nada."
         * - Desarrollo debe revisar: Si el método de búsqueda lanza alguna excepción en lugar de retornar la lista vacía mapeada.
         */
    }

    // =========================================================================
    // actualizarPedido()
    // =========================================================================

    @Test
    @DisplayName("actualizarPedido - Debe actualizar los datos del pedido y retornar el DTO actualizado")
    void actualizarPedido_DebeActualizarCampos_CuandoIdExiste() {
        // ARRANGE
        Long id = 100L;
        Pedido pedidoExistente = Pedido.builder()
                .id(id).clienteId(1L).fechaPedido(LocalDateTime.now()).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(150.0).build();

        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(2L);
        request.setTipoEntrega("LLEVAR");

        Pedido pedidoActualizado = Pedido.builder()
                .id(id).clienteId(2L).fechaPedido(pedidoExistente.getFechaPedido()).tipoEntrega("LLEVAR")
                .estado("CREADO").total(150.0).build();

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedidoExistente));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoActualizado);

        // ACT
        PedidoResponseDTO resultado = pedidoService.actualizarPedido(id, request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(2L, resultado.getClienteId(), "El clienteId debe haberse actualizado");
        assertEquals("LLEVAR", resultado.getTipoEntrega(), "El tipo de entrega debe haberse actualizado");
        assertEquals("CREADO", resultado.getEstado(), "El estado no debe cambiar en este flujo");
        assertEquals(150.0, resultado.getTotal(), "El total no debe verse afectado");

        // VERIFY
        verify(pedidoRepository, times(1)).findById(id);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("actualizarPedido - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarPedido_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        PedidoRequestDTO request = new PedidoRequestDTO();
        request.setClienteId(2L);
        request.setTipoEntrega("LLEVAR");

        when(pedidoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pedidoService.actualizarPedido(idInvalido, request),
                "Debe lanzar ResourceNotFoundException si el pedido no existe"
        );

        assertEquals("Pedido no encontrado con ID: 999", exception.getMessage());

        // VERIFY: no llama a save
        verify(pedidoRepository, times(1)).findById(idInvalido);
        verify(pedidoRepository, never()).save(any(Pedido.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API crea un nuevo pedido en la base de datos al enviar un ID inexistente en la actualización:
         * - QA debe reportar: "Falla al actualizar: La API crea un nuevo pedido en lugar de retornar error 404."
         * - Desarrollo debe revisar: Si el orElseThrow() está antes del save() y no se está invocando el save() de manera incondicional.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe actualizar el estado del pedido y retornar el DTO actualizado")
    void cambiarEstado_DebeActualizarEstado_CuandoIdExiste() {
        // ARRANGE
        Long id = 100L;
        String nuevoEstado = "PAGADO";
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedidoExistente = Pedido.builder()
                .id(id).clienteId(1L).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(150.0).build();

        Pedido pedidoActualizado = Pedido.builder()
                .id(id).clienteId(1L).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado(nuevoEstado).total(150.0).build();

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedidoExistente));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoActualizado);

        // ACT
        PedidoResponseDTO resultado = pedidoService.cambiarEstado(id, nuevoEstado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(nuevoEstado, resultado.getEstado(), "El estado debe haber cambiado a PAGADO");
        assertEquals(150.0, resultado.getTotal(), "El total no debe cambiar");

        // VERIFY
        verify(pedidoRepository, times(1)).findById(id);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pedidoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pedidoService.cambiarEstado(idInvalido, "ENTREGADO"),
                "Debe lanzar ResourceNotFoundException si el pedido no existe"
        );

        assertEquals("Pedido no encontrado con ID: 999", exception.getMessage());

        // VERIFY: no llama a save
        verify(pedidoRepository, times(1)).findById(idInvalido);
        verify(pedidoRepository, never()).save(any(Pedido.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con HTTP 200 en lugar de 404 al cambiar el estado de un pedido inexistente:
         * - QA debe reportar: "Falla al cambiar estado de pedido: Se reporta éxito al actualizar una transacción inexistente."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio antes del save.
         */
    }

    // =========================================================================
    // eliminarPedido()
    // =========================================================================

    @Test
    @DisplayName("eliminarPedido - Debe eliminar el pedido correctamente cuando el ID existe")
    void eliminarPedido_DebeEliminarPedido_CuandoIdExiste() {
        // ARRANGE
        Long id = 100L;
        LocalDateTime ahora = LocalDateTime.now();
        Pedido pedido = Pedido.builder()
                .id(id).clienteId(1L).fechaPedido(ahora).tipoEntrega("DOMICILIO")
                .estado("CREADO").total(150.0).build();

        when(pedidoRepository.findById(id)).thenReturn(Optional.of(pedido));
        doNothing().when(pedidoRepository).delete(pedido);

        // ACT & ASSERT
        assertDoesNotThrow(() -> pedidoService.eliminarPedido(id),
                "No debe lanzar excepción al eliminar un pedido existente");

        // VERIFY
        verify(pedidoRepository, times(1)).findById(id);
        verify(pedidoRepository, times(1)).delete(pedido);
    }

    @Test
    @DisplayName("eliminarPedido - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarPedido_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pedidoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pedidoService.eliminarPedido(idInvalido),
                "Debe lanzar ResourceNotFoundException si el pedido a eliminar no existe"
        );

        assertEquals("Pedido no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pedidoRepository, times(1)).findById(idInvalido);
        verify(pedidoRepository, never()).delete(any(Pedido.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /pedidos/{id} responde HTTP 204 (sin contenido) para un pedido inexistente:
         * - QA debe reportar: "Falla al eliminar pedido: La API confirma la eliminación de un pedido inexistente."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está implementado en la lógica del servicio antes del delete.
         */
    }
}

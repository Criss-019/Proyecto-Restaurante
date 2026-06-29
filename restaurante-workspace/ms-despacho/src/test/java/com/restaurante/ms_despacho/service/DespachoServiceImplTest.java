package com.restaurante.ms_despacho.service;

import com.restaurante.ms_despacho.client.PedidoClient;
import com.restaurante.ms_despacho.dto.DespachoRequestDTO;
import com.restaurante.ms_despacho.dto.DespachoResponseDTO;
import com.restaurante.ms_despacho.entity.Despacho;
import com.restaurante.ms_despacho.repository.DespachoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import com.restaurante.ms_despacho.exception.ResourceNotFoundException;
import feign.FeignException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para DespachoServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (DespachoRepository) y el cliente Feign (PedidoClient) con @Mock.
 * - Se inyectan los mocks en DespachoServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class DespachoServiceImplTest {

    @Mock
    private DespachoRepository despachoRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private DespachoServiceImpl despachoService;

    // =========================================================================
    // programarDespacho()
    // =========================================================================

    @Test
    @DisplayName("programarDespacho - Debe registrar y retornar el despacho mapeado a DTO con estado EN_PREPARACION")
    void programarDespacho_DebeRetornarDespachoResponseDTO_CuandoRequestEsValido() {
        // ARRANGE: Preparar datos de entrada del DTO, entidad simulada y fecha
        LocalDateTime fechaEstimada = LocalDateTime.now().plusHours(2);
        
        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(50L);
        request.setRepartidorAsignado("Carlos Repartidor");
        request.setDireccionEntrega("Av. Principal 123");
        request.setFechaEntregaEstimada(fechaEstimada);

        Despacho despachoGuardado = Despacho.builder()
                .id(1L)
                .pedidoId(50L)
                .repartidorAsignado("Carlos Repartidor")
                .direccionEntrega("Av. Principal 123")
                .estado("EN_PREPARACION")
                .fechaSalida(null)
                .fechaEntregaEstimada(fechaEstimada)
                .fechaEntrega(null)
                .build();

        // Configurar Mockito para simular el comportamiento del repositorio
        when(despachoRepository.save(any(Despacho.class))).thenReturn(despachoGuardado);

        // ACT: Invocar el método del servicio a probar
        DespachoResponseDTO resultado = despachoService.programarDespacho(request);

        // ASSERT: Verificar mapeo y estado inicial de preparacion
        assertNotNull(resultado, "El DTO de respuesta no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID mapeado no coincide");
        assertEquals(50L, resultado.getPedidoId(), "El ID del pedido no coincide");
        assertEquals("Carlos Repartidor", resultado.getRepartidorAsignado(), "El repartidor no coincide");
        assertEquals("Av. Principal 123", resultado.getDireccionEntrega(), "La dirección de entrega no coincide");
        assertEquals("EN_PREPARACION", resultado.getEstado(), "El estado inicial debe ser EN_PREPARACION");
        assertNull(resultado.getFechaSalida(), "La fecha de salida debe ser nula al programar");
        assertEquals(fechaEstimada, resultado.getFechaEntregaEstimada(), "La fecha estimada de entrega no coincide");
        assertNull(resultado.getFechaEntrega(), "La fecha de entrega debe ser nula al programar");

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(despachoRepository, times(1)).save(any(Despacho.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al programar el despacho el sistema lanza un NullPointerException:
         * - QA debe reportar: "Falla al programar despacho: NullPointerException en el flujo de negocio."
         * - Desarrollo debe revisar: Si el builder de la entidad Despacho o el mapToDTO está accediendo a campos nulos del request.
         */
    }

    // =========================================================================
    // obtenerTodos()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodos - Debe retornar la lista de todos los despachos mapeados a DTO")
    void obtenerTodos_DebeRetornarListaDeDespachoResponseDTO() {
        // ARRANGE: Preparar datos simulados en la base de datos
        LocalDateTime ahora = LocalDateTime.now();
        Despacho d1 = Despacho.builder()
                .id(1L).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_PREPARACION").fechaEntregaEstimada(ahora.plusHours(1)).build();
        Despacho d2 = Despacho.builder()
                .id(2L).pedidoId(11L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("EN_RUTA").fechaSalida(ahora).fechaEntregaEstimada(ahora.plusMinutes(45)).build();

        when(despachoRepository.findAll()).thenReturn(java.util.Arrays.asList(d1, d2));

        // ACT: Invocar el método a probar
        java.util.List<DespachoResponseDTO> resultado = despachoService.obtenerTodos();

        // ASSERT: Verificar mapeo correcto y número de elementos
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size(), "El tamaño de la lista de despachos debe ser 2");
        
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("EN_PREPARACION", resultado.get(0).getEstado());
        assertEquals("Juan", resultado.get(0).getRepartidorAsignado());
        
        assertEquals(2L, resultado.get(1).getId());
        assertEquals("EN_RUTA", resultado.get(1).getEstado());
        assertEquals("Pedro", resultado.get(1).getRepartidorAsignado());
        assertNotNull(resultado.get(1).getFechaSalida(), "La fecha de salida no debe ser nula si está en ruta");

        // VERIFY: Validar interacción con repositorio
        verify(despachoRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la lista de despachos se entrega vacía a pesar de haber despachos registrados en el sistema:
         * - QA debe reportar: "Falla al listar despachos: La consulta retorna una lista vacía en producción."
         * - Desarrollo debe revisar: Si el repositorio está correctamente conectado a la base de datos o si la conversión a DTO devuelve null.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el despacho mapeado a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarDespachoResponseDTO_CuandoIdExiste() {
        // ARRANGE: Despacho simulado
        Long idBusqueda = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Despacho despacho = Despacho.builder()
                .id(idBusqueda)
                .pedidoId(10L)
                .repartidorAsignado("Juan")
                .direccionEntrega("Calle A")
                .estado("EN_PREPARACION")
                .fechaEntregaEstimada(ahora.plusHours(1))
                .build();

        when(despachoRepository.findById(idBusqueda)).thenReturn(Optional.of(despacho));

        // ACT: Invocar el método
        DespachoResponseDTO resultado = despachoService.obtenerPorId(idBusqueda);

        // ASSERT: Validar campos
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(idBusqueda, resultado.getId(), "El ID debe coincidir");
        assertEquals(10L, resultado.getPedidoId());
        assertEquals("Juan", resultado.getRepartidorAsignado());
        assertEquals("Calle A", resultado.getDireccionEntrega());
        assertEquals("EN_PREPARACION", resultado.getEstado());

        // VERIFY: findById invocado 1 vez
        verify(despachoRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular que el recurso no existe
        Long idInvalido = 999L;
        when(despachoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> despachoService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException si el despacho no existe"
        );

        assertEquals("Despacho no encontrado con ID: 999", exception.getMessage(),
                "El mensaje de excepción debe contener el ID no encontrado");

        // VERIFY: findById invocado 1 vez
        verify(despachoRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con 500 Server Error en vez de 404 al consultar un ID inexistente:
         * - QA debe reportar: "Falla al buscar despacho inexistente: Responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el ExceptionHandler del controlador captura adecuadamente ResourceNotFoundException.
         */
    }

    // =========================================================================
    // obtenerPorPedidoId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorPedidoId - Debe retornar el despacho mapeado a DTO cuando el pedidoId existe")
    void obtenerPorPedidoId_DebeRetornarDespachoResponseDTO_CuandoPedidoIdExiste() {
        // ARRANGE: Despacho simulado para un pedido
        Long pedidoId = 50L;
        LocalDateTime ahora = LocalDateTime.now();
        Despacho despacho = Despacho.builder()
                .id(1L)
                .pedidoId(pedidoId)
                .repartidorAsignado("Carlos Repartidor")
                .direccionEntrega("Av. Principal 123")
                .estado("EN_PREPARACION")
                .fechaEntregaEstimada(ahora.plusHours(2))
                .build();

        when(despachoRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(despacho));

        // ACT: Invocar el método
        DespachoResponseDTO resultado = despachoService.obtenerPorPedidoId(pedidoId);

        // ASSERT: Validar campos
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(pedidoId, resultado.getPedidoId(), "El pedidoId debe coincidir");
        assertEquals("Carlos Repartidor", resultado.getRepartidorAsignado());
        assertEquals("Av. Principal 123", resultado.getDireccionEntrega());
        assertEquals("EN_PREPARACION", resultado.getEstado());

        // VERIFY: findByPedidoId invocado 1 vez
        verify(despachoRepository, times(1)).findByPedidoId(pedidoId);
    }

    @Test
    @DisplayName("obtenerPorPedidoId - Debe lanzar ResourceNotFoundException cuando el pedidoId no existe")
    void obtenerPorPedidoId_DebeLanzarResourceNotFoundException_CuandoPedidoIdNoExiste() {
        // ARRANGE: Simular que no existe logística de despacho para el pedido
        Long pedidoIdInvalido = 999L;
        when(despachoRepository.findByPedidoId(pedidoIdInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> despachoService.obtenerPorPedidoId(pedidoIdInvalido),
                "Debe lanzar ResourceNotFoundException si no hay despacho para el pedido"
        );

        assertEquals("No se encontró logística de despacho para el pedido ID: 999", exception.getMessage(),
                "El mensaje de excepción debe contener el ID de pedido no encontrado");

        // VERIFY: findByPedidoId invocado 1 vez
        verify(despachoRepository, times(1)).findByPedidoId(pedidoIdInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con una lista vacía o HTTP 200 con cuerpo nulo en lugar de 404 al buscar logística de un pedido inexistente:
         * - QA debe reportar: "Falla al buscar despacho de pedido: Retorna 200 en lugar de exception 404."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio.
         */
    }

    // =========================================================================
    // actualizarDespacho()
    // =========================================================================

    @Test
    @DisplayName("actualizarDespacho - Debe actualizar campos y NO llamar Feign cuando el estado no es ENTREGADO")
    void actualizarDespacho_DebeActualizarCampos_CuandoIdExisteYEstadoNoEsEntregado() {
        // ARRANGE: Despacho existente y request con estado EN_RUTA
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Despacho existente = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_PREPARACION").fechaEntregaEstimada(ahora.plusHours(1)).build();

        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(10L);
        request.setRepartidorAsignado("Pedro");
        request.setDireccionEntrega("Calle B");
        request.setEstado("EN_RUTA");
        request.setFechaEntregaEstimada(ahora.plusMinutes(45));

        Despacho actualizado = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("EN_RUTA").fechaEntregaEstimada(ahora.plusMinutes(45)).build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(actualizado);

        // ACT: Actualizar
        DespachoResponseDTO resultado = despachoService.actualizarDespacho(id, request);

        // ASSERT: Verificar cambios
        assertNotNull(resultado);
        assertEquals("Pedro", resultado.getRepartidorAsignado());
        assertEquals("Calle B", resultado.getDireccionEntrega());
        assertEquals("EN_RUTA", resultado.getEstado());

        // VERIFY: Verificar llamadas
        verify(despachoRepository, times(1)).findById(id);
        verify(despachoRepository, times(1)).save(any(Despacho.class));
        verifyNoInteractions(pedidoClient); // No debe llamar a Feign
    }

    @Test
    @DisplayName("actualizarDespacho - Debe actualizar campos, marcar fecha de entrega y llamar Feign cuando el estado es ENTREGADO")
    void actualizarDespacho_DebeLlamarFeign_CuandoIdExisteYEstadoEsEntregado() {
        // ARRANGE: Despacho existente y request con estado ENTREGADO
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Despacho existente = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("EN_RUTA").fechaEntregaEstimada(ahora.plusMinutes(45)).build();

        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(10L);
        request.setRepartidorAsignado("Pedro");
        request.setDireccionEntrega("Calle B");
        request.setEstado("ENTREGADO");
        request.setFechaEntregaEstimada(ahora.plusMinutes(45));

        Despacho actualizado = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("ENTREGADO").fechaEntregaEstimada(ahora.plusMinutes(45))
                .fechaEntrega(ahora).build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(actualizado);
        doNothing().when(pedidoClient).cambiarEstadoPedido(10L, "ENTREGADO");

        // ACT: Actualizar
        DespachoResponseDTO resultado = despachoService.actualizarDespacho(id, request);

        // ASSERT: Verificar estado y fecha de entrega no nula
        assertNotNull(resultado);
        assertEquals("ENTREGADO", resultado.getEstado());
        assertNotNull(resultado.getFechaEntrega());

        // VERIFY: Verificar llamadas
        verify(despachoRepository, times(1)).findById(id);
        verify(pedidoClient, times(1)).cambiarEstadoPedido(10L, "ENTREGADO");
        verify(despachoRepository, times(1)).save(any(Despacho.class));
    }

    @Test
    @DisplayName("actualizarDespacho - Debe capturar FeignException, registrar el error y continuar guardando el despacho")
    void actualizarDespacho_DebeContinuar_CuandoFeignLanzaException() {
        // ARRANGE: Despacho existente y request ENTREGADO, pero Feign falla
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Despacho existente = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("EN_RUTA").fechaEntregaEstimada(ahora.plusMinutes(45)).build();

        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(10L);
        request.setRepartidorAsignado("Pedro");
        request.setDireccionEntrega("Calle B");
        request.setEstado("ENTREGADO");
        request.setFechaEntregaEstimada(ahora.plusMinutes(45));

        Despacho actualizado = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Pedro").direccionEntrega("Calle B")
                .estado("ENTREGADO").fechaEntregaEstimada(ahora.plusMinutes(45))
                .fechaEntrega(ahora).build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(actualizado);
        
        // Simular que Feign falla
        FeignException mockException = mock(FeignException.class);
        when(mockException.getMessage()).thenReturn("Internal Server Error");
        doThrow(mockException).when(pedidoClient).cambiarEstadoPedido(10L, "ENTREGADO");

        // ACT: Ejecutar actualización
        DespachoResponseDTO resultado = despachoService.actualizarDespacho(id, request);

        // ASSERT: Aunque falló Feign, el despacho debe haberse actualizado en BD
        assertNotNull(resultado);
        assertEquals("ENTREGADO", resultado.getEstado());

        // VERIFY
        verify(despachoRepository, times(1)).findById(id);
        verify(pedidoClient, times(1)).cambiarEstadoPedido(10L, "ENTREGADO");
        verify(despachoRepository, times(1)).save(any(Despacho.class)); // Se guarda a pesar de la falla de Feign
    }

    @Test
    @DisplayName("actualizarDespacho - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarDespacho_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Despacho inexistente
        Long idInvalido = 999L;
        DespachoRequestDTO request = new DespachoRequestDTO();
        request.setPedidoId(10L);
        request.setRepartidorAsignado("Pedro");
        request.setDireccionEntrega("Calle B");
        request.setEstado("ENTREGADO");

        when(despachoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> despachoService.actualizarDespacho(idInvalido, request),
                "Debe lanzar ResourceNotFoundException si el despacho no existe"
        );

        assertEquals("Despacho no encontrado con ID: 999", exception.getMessage());

        // VERIFY: No se toca el save ni pedidoClient
        verify(despachoRepository, times(1)).findById(idInvalido);
        verify(despachoRepository, never()).save(any(Despacho.class));
        verifyNoInteractions(pedidoClient);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si cuando Feign falla al avisar a ms-pedidos se cae todo el flujo de despacho (HTTP 500 para el cliente):
         * - QA debe reportar: "Falla de resiliencia: Si el microservicio de pedidos está caído, el despacho no se puede marcar como entregado."
         * - Desarrollo debe revisar: Si el try-catch de FeignException está capturando correctamente la excepción en actualizarDespacho.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe cambiar a EN_RUTA y asignar fechaSalida cuando el estado es EN_RUTA")
    void cambiarEstado_DebeEstablecerFechaSalida_CuandoEstadoEsEnRuta() {
        // ARRANGE: Despacho existente en preparacion
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Despacho existente = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_PREPARACION").fechaEntregaEstimada(ahora.plusHours(1)).build();

        Despacho actualizado = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_RUTA").fechaSalida(ahora).fechaEntregaEstimada(ahora.plusHours(1)).build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(actualizado);

        // ACT: Cambiar estado a EN_RUTA
        DespachoResponseDTO resultado = despachoService.cambiarEstado(id, "EN_RUTA");

        // ASSERT: Verificar que se actualizó el estado y que tiene fechaSalida
        assertNotNull(resultado);
        assertEquals("EN_RUTA", resultado.getEstado());
        assertNotNull(resultado.getFechaSalida(), "La fecha de salida debe registrarse al pasar a ruta");

        // VERIFY
        verify(despachoRepository, times(1)).findById(id);
        verify(despachoRepository, times(1)).save(any(Despacho.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe cambiar de estado sin establecer fechaSalida cuando el estado es RECHAZADO")
    void cambiarEstado_DebeActualizarEstadoSinFechaSalida_CuandoEstadoNoEsEnRuta() {
        // ARRANGE: Despacho existente
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Despacho existente = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_PREPARACION").fechaEntregaEstimada(ahora.plusHours(1)).build();

        Despacho actualizado = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("RECHAZADO").fechaEntregaEstimada(ahora.plusHours(1)).build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(existente));
        when(despachoRepository.save(any(Despacho.class))).thenReturn(actualizado);

        // ACT: Cambiar estado
        DespachoResponseDTO resultado = despachoService.cambiarEstado(id, "RECHAZADO");

        // ASSERT: Estado actualizado, fechaSalida nula
        assertNotNull(resultado);
        assertEquals("RECHAZADO", resultado.getEstado());
        assertNull(resultado.getFechaSalida(), "La fecha de salida debe ser nula si no está en ruta");

        // VERIFY
        verify(despachoRepository, times(1)).findById(id);
        verify(despachoRepository, times(1)).save(any(Despacho.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular ID inválido
        Long idInvalido = 999L;
        when(despachoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Lanzar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> despachoService.cambiarEstado(idInvalido, "EN_RUTA"),
                "Debe lanzar excepción si el ID no existe"
        );

        assertEquals("Despacho no encontrado con ID: 999", exception.getMessage());

        // VERIFY: delete o save no deben tocarse
        verify(despachoRepository, times(1)).findById(idInvalido);
        verify(despachoRepository, never()).save(any(Despacho.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el despacho cambia su estado a EN_RUTA pero la fecha_salida se guarda nula en la base de datos:
         * - QA debe reportar: "Falla logística: El despacho figura en ruta pero la fecha y hora de salida no se registra."
         * - Desarrollo debe revisar: Si el condicional de estado 'EN_RUTA' en el método cambiarEstado es sensible a mayúsculas/minúsculas o no se está ejecutando.
         */
    }

    // =========================================================================
    // eliminarDespacho()
    // =========================================================================

    @Test
    @DisplayName("eliminarDespacho - Debe eliminar el despacho cuando el ID existe")
    void eliminarDespacho_DebeEliminarDespacho_CuandoIdExiste() {
        // ARRANGE: Despacho existente
        Long id = 1L;
        Despacho despacho = Despacho.builder()
                .id(id).pedidoId(10L).repartidorAsignado("Juan").direccionEntrega("Calle A")
                .estado("EN_PREPARACION").build();

        when(despachoRepository.findById(id)).thenReturn(Optional.of(despacho));
        doNothing().when(despachoRepository).delete(despacho);

        // ACT: Llamar a eliminar
        assertDoesNotThrow(() -> despachoService.eliminarDespacho(id),
                "No debe lanzar ninguna excepción al eliminar un despacho existente");

        // VERIFY: Verificar llamadas findById + delete
        verify(despachoRepository, times(1)).findById(id);
        verify(despachoRepository, times(1)).delete(despacho);
    }

    @Test
    @DisplayName("eliminarDespacho - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarDespacho_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular ID inexistente
        Long idInvalido = 999L;
        when(despachoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Lanzar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> despachoService.eliminarDespacho(idInvalido),
                "Debe lanzar excepción si el ID no existe"
        );

        assertEquals("Despacho no encontrado con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, delete NO debe haberse invocado
        verify(despachoRepository, times(1)).findById(idInvalido);
        verify(despachoRepository, never()).delete(any(Despacho.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar eliminar un despacho inexistente la API devuelve HTTP 200 en lugar de 404:
         * - QA debe reportar: "Falla al eliminar despacho: Se reporta éxito al eliminar un despacho que no existe."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio antes del delete.
         */
    }
}





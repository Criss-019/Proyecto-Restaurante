package com.restaurante.ms_pagos.service;

import com.restaurante.ms_pagos.client.FacturacionClient;
import com.restaurante.ms_pagos.client.PedidoClient;
import com.restaurante.ms_pagos.client.dto.FacturaRequestDTO;
import com.restaurante.ms_pagos.dto.PagoRequestDTO;
import com.restaurante.ms_pagos.dto.PagoResponseDTO;
import com.restaurante.ms_pagos.entity.Pago;
import com.restaurante.ms_pagos.exception.ResourceNotFoundException;
import com.restaurante.ms_pagos.repository.PagoRepository;
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
 * Pruebas unitarias para PagoServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simulan la base de datos (PagoRepository) y clientes Feign (PedidoClient, FacturacionClient).
 * - Se inyectan los mocks en PagoServiceImpl.
 * - Estructura: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoClient pedidoClient;

    @Mock
    private FacturacionClient facturacionClient;

    @InjectMocks
    private PagoServiceImpl pagoService;

    // =========================================================================
    // registrarPago()
    // =========================================================================

    @Test
    @DisplayName("registrarPago - Debe guardar el pago localmente y notificar a ms-pedidos y ms-facturacion con éxito")
    void registrarPago_DebeRetornarPagoResponseDTO_CuandoFlujoEsExitoso() {
        // ARRANGE
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(10L);
        request.setMonto(15000.0);
        request.setMetodoPago("TARJETA");

        Pago pagoGuardado = Pago.builder()
                .id(1L)
                .pedidoId(10L)
                .monto(15000.0)
                .metodoPago("TARJETA")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        doNothing().when(pedidoClient).cambiarEstadoPedido(10L, "PAGADO");
        doNothing().when(facturacionClient).emitirFactura(any(FacturaRequestDTO.class));

        // ACT
        PagoResponseDTO resultado = pagoService.registrarPago(request);

        // ASSERT
        assertNotNull(resultado, "El pago retornado no debe ser nulo");
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getPedidoId());
        assertEquals(15000.0, resultado.getMonto());
        assertEquals("TARJETA", resultado.getMetodoPago());
        assertEquals("APROBADO", resultado.getEstado());
        assertNotNull(resultado.getFechaPago());

        // VERIFY
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(pedidoClient, times(1)).cambiarEstadoPedido(10L, "PAGADO");
        verify(facturacionClient, times(1)).emitirFactura(any(FacturaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el pago es registrado localmente pero los llamados Feign nunca se realizan:
         * - QA debe reportar: "Falla de integración: El pago se aprueba pero el pedido no cambia a PAGADO ni se emite factura."
         * - Desarrollo debe revisar: Si el bloque try/catch de Feign no está silenciando llamadas o si las interfaces de FeignClient están configuradas incorrectamente.
         */
    }

    @Test
    @DisplayName("registrarPago - Debe guardar el pago localmente aun cuando ms-pedidos falle con FeignException")
    void registrarPago_DebeRetornarPagoResponseDTO_CuandoMsPedidosFalla() {
        // ARRANGE
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(10L);
        request.setMonto(15000.0);
        request.setMetodoPago("TARJETA");

        Pago pagoGuardado = Pago.builder()
                .id(1L)
                .pedidoId(10L)
                .monto(15000.0)
                .metodoPago("TARJETA")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // Simular fallo de Feign en PedidoClient
        feign.FeignException mockException = mock(feign.FeignException.class);
        when(mockException.getMessage()).thenReturn("Timeout connecting to ms-pedidos");
        doThrow(mockException).when(pedidoClient).cambiarEstadoPedido(10L, "PAGADO");
        
        // Simular éxito en FacturacionClient
        doNothing().when(facturacionClient).emitirFactura(any(FacturaRequestDTO.class));

        // ACT
        PagoResponseDTO resultado = pagoService.registrarPago(request);

        // ASSERT
        assertNotNull(resultado, "El pago debe registrarse localmente a pesar del fallo externo");
        assertEquals("APROBADO", resultado.getEstado());

        // VERIFY
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(pedidoClient, times(1)).cambiarEstadoPedido(10L, "PAGADO");
        verify(facturacionClient, times(1)).emitirFactura(any(FacturaRequestDTO.class));
    }

    @Test
    @DisplayName("registrarPago - Debe guardar el pago localmente aun cuando ms-facturacion falle con FeignException")
    void registrarPago_DebeRetornarPagoResponseDTO_CuandoMsFacturacionFalla() {
        // ARRANGE
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(10L);
        request.setMonto(15000.0);
        request.setMetodoPago("TARJETA");

        Pago pagoGuardado = Pago.builder()
                .id(1L)
                .pedidoId(10L)
                .monto(15000.0)
                .metodoPago("TARJETA")
                .fechaPago(LocalDateTime.now())
                .estado("APROBADO")
                .build();

        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoGuardado);
        
        // Simular éxito en PedidoClient
        doNothing().when(pedidoClient).cambiarEstadoPedido(10L, "PAGADO");
        
        // Simular fallo de Feign en FacturacionClient
        feign.FeignException mockException = mock(feign.FeignException.class);
        when(mockException.getMessage()).thenReturn("ms-facturacion offline");
        doThrow(mockException).when(facturacionClient).emitirFactura(any(FacturaRequestDTO.class));

        // ACT
        PagoResponseDTO resultado = pagoService.registrarPago(request);

        // ASSERT
        assertNotNull(resultado, "El pago debe registrarse localmente a pesar del fallo externo");
        assertEquals("APROBADO", resultado.getEstado());

        // VERIFY
        verify(pagoRepository, times(1)).save(any(Pago.class));
        verify(pedidoClient, times(1)).cambiarEstadoPedido(10L, "PAGADO");
        verify(facturacionClient, times(1)).emitirFactura(any(FacturaRequestDTO.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si cuando falla la facturación todo el proceso de registro de pago aborta y lanza excepción:
         * - QA debe reportar: "Falla de resiliencia: Si el microservicio de facturación está inactivo, no se puede cobrar el pedido."
         * - Desarrollo debe revisar: Si el bloque try/catch correspondiente a la facturación está manejando correctamente FeignException.
         */
    }

    // =========================================================================
    // obtenerTodos()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodos - Debe retornar una lista con todos los pagos registrados mapeados a DTO")
    void obtenerTodos_DebeRetornarListaDePagoResponseDTO() {
        // ARRANGE: Preparar dos pagos
        LocalDateTime ahora = LocalDateTime.now();
        Pago p1 = Pago.builder()
                .id(1L).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado("APROBADO").build();

        Pago p2 = Pago.builder()
                .id(2L).pedidoId(11L).monto(8500.0).metodoPago("EFECTIVO")
                .fechaPago(ahora).estado("RECHAZADO").build();

        when(pagoRepository.findAll()).thenReturn(java.util.Arrays.asList(p1, p2));

        // ACT
        java.util.List<PagoResponseDTO> resultado = pagoService.obtenerTodos();

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size());

        assertEquals(1L, resultado.get(0).getId());
        assertEquals("TARJETA", resultado.get(0).getMetodoPago());
        assertEquals("APROBADO", resultado.get(0).getEstado());

        assertEquals(2L, resultado.get(1).getId());
        assertEquals("EFECTIVO", resultado.get(1).getMetodoPago());
        assertEquals("RECHAZADO", resultado.get(1).getEstado());

        // VERIFY
        verify(pagoRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el listado general de pagos arroja una lista vacía cuando existen transacciones en la base de datos:
         * - QA debe reportar: "Falla al listar pagos: El endpoint GET /pagos no devuelve registros."
         * - Desarrollo debe revisar: Si la llamada al repositorio findAll() o el mapeo stream/mapToDTO están retornando una lista sin elementos.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el pago mapeado a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarPagoResponseDTO_CuandoIdExiste() {
        // ARRANGE
        Long idBusqueda = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Pago pago = Pago.builder()
                .id(idBusqueda).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado("APROBADO").build();

        when(pagoRepository.findById(idBusqueda)).thenReturn(Optional.of(pago));

        // ACT
        PagoResponseDTO resultado = pagoService.obtenerPorId(idBusqueda);

        // ASSERT
        assertNotNull(resultado, "El pago retornado no debe ser nulo");
        assertEquals(idBusqueda, resultado.getId());
        assertEquals(10L, resultado.getPedidoId());
        assertEquals(15000.0, resultado.getMonto());
        assertEquals("TARJETA", resultado.getMetodoPago());
        assertEquals("APROBADO", resultado.getEstado());

        // VERIFY
        verify(pagoRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pagoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagoService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException si el pago no existe"
        );

        assertEquals("Pago no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pagoRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar un pago por ID inexistente la API devuelve HTTP 500 en lugar de 404 Not Found:
         * - QA debe reportar: "Falla al buscar pago inexistente: Responde con 500 en lugar de 404."
         * - Desarrollo debe revisar: Si el GlobalExceptionHandler del módulo ms-pagos está interceptando correctamente ResourceNotFoundException.
         */
    }

    // =========================================================================
    // obtenerPorPedidoId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorPedidoId - Debe retornar la lista de pagos asociados al pedidoId")
    void obtenerPorPedidoId_DebeRetornarListaDePagoResponseDTO_CuandoPedidoIdExiste() {
        // ARRANGE
        Long pedidoId = 10L;
        LocalDateTime ahora = LocalDateTime.now();
        Pago p1 = Pago.builder()
                .id(1L).pedidoId(pedidoId).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado("APROBADO").build();

        when(pagoRepository.findByPedidoId(pedidoId)).thenReturn(java.util.Arrays.asList(p1));

        // ACT
        java.util.List<PagoResponseDTO> resultado = pagoService.obtenerPorPedidoId(pedidoId);

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(1, resultado.size());
        assertEquals(pedidoId, resultado.get(0).getPedidoId());
        assertEquals(15000.0, resultado.get(0).getMonto());
        assertEquals("TARJETA", resultado.get(0).getMetodoPago());

        // VERIFY
        verify(pagoRepository, times(1)).findByPedidoId(pedidoId);
    }

    @Test
    @DisplayName("obtenerPorPedidoId - Debe retornar una lista vacía cuando el pedidoId no tiene pagos")
    void obtenerPorPedidoId_DebeRetornarListaVacia_CuandoPedidoIdNoTienePagos() {
        // ARRANGE
        Long pedidoIdInvalido = 999L;
        when(pagoRepository.findByPedidoId(pedidoIdInvalido)).thenReturn(java.util.Collections.emptyList());

        // ACT
        java.util.List<PagoResponseDTO> resultado = pagoService.obtenerPorPedidoId(pedidoIdInvalido);

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía para un pedido sin pagos");

        // VERIFY
        verify(pagoRepository, times(1)).findByPedidoId(pedidoIdInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API devuelve error HTTP 404 en lugar de una lista vacía [] al buscar pagos de un pedido que aún no ha pagado:
         * - QA debe reportar: "Falla de consulta: La API devuelve 404 al buscar historial de pagos de un pedido sin transacciones."
         * - Desarrollo debe revisar: Si se está arrojando alguna excepción al no encontrar elementos, en lugar de retornar la lista vacía mapeada.
         */
    }

    // =========================================================================
    // actualizarPago()
    // =========================================================================

    @Test
    @DisplayName("actualizarPago - Debe actualizar los datos del pago y retornar el DTO actualizado sin cambiar fecha ni estado")
    void actualizarPago_DebeActualizarDatosYRetornarDTO_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        LocalDateTime fechaOriginal = LocalDateTime.now().minusDays(1);
        Pago pagoExistente = Pago.builder()
                .id(id).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(fechaOriginal).estado("APROBADO").build();

        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(12L);
        request.setMonto(18000.0);
        request.setMetodoPago("EFECTIVO");

        Pago pagoActualizado = Pago.builder()
                .id(id).pedidoId(12L).monto(18000.0).metodoPago("EFECTIVO")
                .fechaPago(fechaOriginal).estado("APROBADO").build();

        when(pagoRepository.findById(id)).thenReturn(Optional.of(pagoExistente));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoActualizado);

        // ACT
        PagoResponseDTO resultado = pagoService.actualizarPago(id, request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(12L, resultado.getPedidoId());
        assertEquals(18000.0, resultado.getMonto());
        assertEquals("EFECTIVO", resultado.getMetodoPago());
        assertEquals(fechaOriginal, resultado.getFechaPago(), "La fecha de pago original debe permanecer intacta por auditoría");
        assertEquals("APROBADO", resultado.getEstado(), "El estado del pago no debe cambiar en este flujo de actualización básica");

        // VERIFY
        verify(pagoRepository, times(1)).findById(id);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    @DisplayName("actualizarPago - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarPago_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        PagoRequestDTO request = new PagoRequestDTO();
        request.setPedidoId(12L);
        request.setMonto(18000.0);
        request.setMetodoPago("EFECTIVO");

        when(pagoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagoService.actualizarPago(idInvalido, request),
                "Debe lanzar ResourceNotFoundException si el pago no existe"
        );

        assertEquals("Pago no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pagoRepository, times(1)).findById(idInvalido);
        verify(pagoRepository, never()).save(any(Pago.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el sistema modifica la fecha de pago original al actualizar el monto:
         * - QA debe reportar: "Falla de auditoría contable: La fecha de pago original fue alterada tras una actualización."
         * - Desarrollo debe revisar: Si el método actualizarPago está llamando a setFechaPago(LocalDateTime.now()) de forma inadvertida.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe cambiar el estado del pago y retornar el DTO actualizado")
    void cambiarEstado_DebeCambiarEstadoYRetornarDTO_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        String nuevoEstado = "FALLIDO";
        LocalDateTime ahora = LocalDateTime.now();
        Pago pagoExistente = Pago.builder()
                .id(id).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado("APROBADO").build();

        Pago pagoActualizado = Pago.builder()
                .id(id).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado(nuevoEstado).build();

        when(pagoRepository.findById(id)).thenReturn(Optional.of(pagoExistente));
        when(pagoRepository.save(any(Pago.class))).thenReturn(pagoActualizado);

        // ACT
        PagoResponseDTO resultado = pagoService.cambiarEstado(id, nuevoEstado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(nuevoEstado, resultado.getEstado(), "El estado debe haber sido actualizado a FALLIDO");
        assertEquals(15000.0, resultado.getMonto());

        // VERIFY
        verify(pagoRepository, times(1)).findById(id);
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pagoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagoService.cambiarEstado(idInvalido, "RECHAZADO"),
                "Debe lanzar ResourceNotFoundException si el pago no existe"
        );

        assertEquals("Pago no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pagoRepository, times(1)).findById(idInvalido);
        verify(pagoRepository, never()).save(any(Pago.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde HTTP 200 en lugar de 404 al cambiar el estado de un pago inexistente:
         * - QA debe reportar: "Falla al cambiar estado de pago: Se reporta éxito al actualizar una transacción inexistente."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio antes del save.
         */
    }

    // =========================================================================
    // eliminarPago()
    // =========================================================================

    @Test
    @DisplayName("eliminarPago - Debe eliminar el pago correctamente cuando el ID existe")
    void eliminarPago_DebeEliminarPago_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Pago pago = Pago.builder()
                .id(id).pedidoId(10L).monto(15000.0).metodoPago("TARJETA")
                .fechaPago(ahora).estado("APROBADO").build();

        when(pagoRepository.findById(id)).thenReturn(Optional.of(pago));
        doNothing().when(pagoRepository).delete(pago);

        // ACT & ASSERT
        assertDoesNotThrow(() -> pagoService.eliminarPago(id),
                "No debe lanzar excepción al eliminar un pago existente");

        // VERIFY
        verify(pagoRepository, times(1)).findById(id);
        verify(pagoRepository, times(1)).delete(pago);
    }

    @Test
    @DisplayName("eliminarPago - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarPago_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(pagoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pagoService.eliminarPago(idInvalido),
                "Debe lanzar ResourceNotFoundException si el pago a eliminar no existe"
        );

        assertEquals("Pago no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(pagoRepository, times(1)).findById(idInvalido);
        verify(pagoRepository, never()).delete(any(Pago.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /pagos/{id} responde HTTP 204 (sin contenido) para un pago inexistente:
         * - QA debe reportar: "Falla al eliminar pago: La API confirma la eliminación de un pago inexistente."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está implementado en la lógica del servicio antes del delete.
         */
    }
}

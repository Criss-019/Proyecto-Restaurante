package com.restaurante.ms_facturacion.service;

import com.restaurante.ms_facturacion.dto.FacturaRequestDTO;
import com.restaurante.ms_facturacion.dto.FacturaResponseDTO;
import com.restaurante.ms_facturacion.entity.Factura;
import com.restaurante.ms_facturacion.exception.ResourceNotFoundException;
import com.restaurante.ms_facturacion.repository.FacturaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para FacturaServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (FacturaRepository) con @Mock.
 * - Se inyectan los mocks en FacturaServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class FacturaServiceImplTest {

    @Mock
    private FacturaRepository facturaRepository;

    @InjectMocks
    private FacturaServiceImpl facturaService;

    // Tasa impositiva usada en la implementación
    private static final double PORCENTAJE_IMPUESTO = 0.19;

    // =========================================================================
    // emitirFactura()
    // =========================================================================

    @Test
    @DisplayName("emitirFactura - Debe calcular impuestos, total, generar folio y retornar el DTO con estado EMITIDA")
    void emitirFactura_DebeRetornarFacturaResponseDTO_ConCalculosFiscalesCorrectos() {
        // ARRANGE: Preparar el request con subtotal de prueba
        double subtotal = 100.0;
        double impuestosEsperados = subtotal * PORCENTAJE_IMPUESTO;   // 19.0
        double totalEsperado = subtotal + impuestosEsperados;          // 119.0

        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setPedidoId(10L);
        request.setSubtotal(subtotal);

        // Simular la factura que devuelve el repositorio tras el guardado
        Factura facturaGuardada = Factura.builder()
                .id(1L)
                .pedidoId(10L)
                .numeroFactura("FAC-ABCD1234")
                .subtotal(subtotal)
                .impuestos(impuestosEsperados)
                .total(totalEsperado)
                .fechaEmision(LocalDateTime.now())
                .estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaGuardada);

        // ACT: Invocar el método del servicio
        FacturaResponseDTO resultado = facturaService.emitirFactura(request);

        // ASSERT: Verificar campos mapeados y cálculos fiscales
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID no coincide");
        assertEquals(10L, resultado.getPedidoId(), "El pedidoId no coincide");
        assertNotNull(resultado.getNumeroFactura(), "El número de folio fiscal no debe ser nulo");
        assertTrue(resultado.getNumeroFactura().startsWith("FAC-"), "El folio debe comenzar con 'FAC-'");
        assertEquals(subtotal, resultado.getSubtotal(), 0.001, "El subtotal no coincide");
        assertEquals(impuestosEsperados, resultado.getImpuestos(), 0.001, "Los impuestos calculados no coinciden");
        assertEquals(totalEsperado, resultado.getTotal(), 0.001, "El total calculado no coincide");
        assertEquals("EMITIDA", resultado.getEstadoFiscal(), "El estado fiscal inicial debe ser EMITIDA");
        assertNotNull(resultado.getFechaEmision(), "La fecha de emisión no debe ser nula");
        assertNotNull(resultado.getUrlPdf(), "La URL del PDF no debe ser nula");

        // VERIFY: Validar que se invocó save() una única vez
        verify(facturaRepository, times(1)).save(any(Factura.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el total de la factura no coincide con subtotal + impuestos:
         * - QA debe reportar: "Falla en cálculo fiscal: El total facturado no corresponde a subtotal + IVA."
         * - Desarrollo debe revisar: Si la constante PORCENTAJE_IMPUESTO está siendo aplicada correctamente
         *   y si el total se calcula como subtotal + impuestos antes de llamar al save().
         */
    }

    // =========================================================================
    // obtenerTodas()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodas - Debe retornar la lista de todas las facturas mapeadas a DTO")
    void obtenerTodas_DebeRetornarListaDeFacturaResponseDTO() {
        // ARRANGE: Preparar dos facturas simuladas con distintos estados fiscales
        LocalDateTime ahora = LocalDateTime.now();

        Factura f1 = Factura.builder()
                .id(1L).pedidoId(10L).numeroFactura("FAC-AAA11111")
                .subtotal(100.0).impuestos(19.0).total(119.0)
                .fechaEmision(ahora).estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-AAA11111.pdf")
                .build();

        Factura f2 = Factura.builder()
                .id(2L).pedidoId(11L).numeroFactura("FAC-BBB22222")
                .subtotal(200.0).impuestos(38.0).total(238.0)
                .fechaEmision(ahora).estadoFiscal("ANULADA")
                .urlPdf("https://restaurante.com/facturas/FAC-BBB22222.pdf")
                .build();

        when(facturaRepository.findAll()).thenReturn(java.util.Arrays.asList(f1, f2));

        // ACT: Invocar el método del servicio
        List<FacturaResponseDTO> resultado = facturaService.obtenerTodas();

        // ASSERT: Verificar tamaño de lista y mapeo de campos clave
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El tamaño de la lista de facturas debe ser 2");

        assertEquals(1L, resultado.get(0).getId());
        assertEquals("FAC-AAA11111", resultado.get(0).getNumeroFactura());
        assertEquals("EMITIDA", resultado.get(0).getEstadoFiscal());
        assertEquals(119.0, resultado.get(0).getTotal(), 0.001);

        assertEquals(2L, resultado.get(1).getId());
        assertEquals("FAC-BBB22222", resultado.get(1).getNumeroFactura());
        assertEquals("ANULADA", resultado.get(1).getEstadoFiscal());
        assertEquals(238.0, resultado.get(1).getTotal(), 0.001);

        // VERIFY: findAll() invocado exactamente una vez
        verify(facturaRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la lista retornada está vacía a pesar de que existen facturas registradas:
         * - QA debe reportar: "Falla al listar facturas: La consulta fiscal retorna lista vacía en el endpoint GET /facturas."
         * - Desarrollo debe revisar: Si el mapToDTO está siendo invocado dentro del stream y si findAll() está configurado correctamente.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar la factura mapeada a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarFacturaResponseDTO_CuandoIdExiste() {
        // ARRANGE: Factura simulada en base de datos
        Long idBusqueda = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Factura factura = Factura.builder()
                .id(idBusqueda)
                .pedidoId(10L)
                .numeroFactura("FAC-ABCD1234")
                .subtotal(100.0)
                .impuestos(19.0)
                .total(119.0)
                .fechaEmision(ahora)
                .estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.findById(idBusqueda)).thenReturn(Optional.of(factura));

        // ACT: Invocar el método
        FacturaResponseDTO resultado = facturaService.obtenerPorId(idBusqueda);

        // ASSERT: Verificar que todos los campos estén correctamente mapeados
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(idBusqueda, resultado.getId(), "El ID no coincide");
        assertEquals(10L, resultado.getPedidoId(), "El pedidoId no coincide");
        assertEquals("FAC-ABCD1234", resultado.getNumeroFactura(), "El número de factura no coincide");
        assertEquals(100.0, resultado.getSubtotal(), 0.001);
        assertEquals(19.0, resultado.getImpuestos(), 0.001);
        assertEquals(119.0, resultado.getTotal(), 0.001);
        assertEquals("EMITIDA", resultado.getEstadoFiscal(), "El estado fiscal no coincide");
        assertNotNull(resultado.getFechaEmision(), "La fecha de emisión no debe ser nula");
        assertNotNull(resultado.getUrlPdf(), "La URL del PDF no debe ser nula");

        // VERIFY: findById invocado 1 vez con el ID correcto
        verify(facturaRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular ausencia del documento fiscal
        Long idInvalido = 999L;
        when(facturaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facturaService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando la factura no existe"
        );

        assertEquals("Factura no encontrada con ID: 999", exception.getMessage(),
                "El mensaje de excepción debe indicar el ID buscado");

        // VERIFY: findById invocado 1 vez, sin más interacciones
        verify(facturaRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con HTTP 500 en lugar de 404 al consultar una factura inexistente:
         * - QA debe reportar: "Falla al buscar factura: La API responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el @ExceptionHandler para ResourceNotFoundException está configurado
         *   en el GlobalExceptionHandler del módulo ms-facturacion.
         */
    }

    // =========================================================================
    // obtenerPorPedidoId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorPedidoId - Debe retornar la factura mapeada a DTO cuando el pedidoId existe")
    void obtenerPorPedidoId_DebeRetornarFacturaResponseDTO_CuandoPedidoIdExiste() {
        // ARRANGE: Factura vinculada a un pedido específico
        Long pedidoId = 10L;
        LocalDateTime ahora = LocalDateTime.now();

        Factura factura = Factura.builder()
                .id(1L)
                .pedidoId(pedidoId)
                .numeroFactura("FAC-ABCD1234")
                .subtotal(150.0)
                .impuestos(28.5)
                .total(178.5)
                .fechaEmision(ahora)
                .estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(factura));

        // ACT: Invocar el método
        FacturaResponseDTO resultado = facturaService.obtenerPorPedidoId(pedidoId);

        // ASSERT: Verificar campos clave del documento fiscal
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(pedidoId, resultado.getPedidoId(), "El pedidoId debe coincidir");
        assertEquals("FAC-ABCD1234", resultado.getNumeroFactura());
        assertEquals(150.0, resultado.getSubtotal(), 0.001);
        assertEquals(28.5, resultado.getImpuestos(), 0.001);
        assertEquals(178.5, resultado.getTotal(), 0.001);
        assertEquals("EMITIDA", resultado.getEstadoFiscal());

        // VERIFY: findByPedidoId invocado 1 vez
        verify(facturaRepository, times(1)).findByPedidoId(pedidoId);
    }

    @Test
    @DisplayName("obtenerPorPedidoId - Debe lanzar ResourceNotFoundException cuando el pedidoId no tiene factura asociada")
    void obtenerPorPedidoId_DebeLanzarResourceNotFoundException_CuandoPedidoIdNoExiste() {
        // ARRANGE: Simular que no hay documento fiscal para ese pedido
        Long pedidoIdInvalido = 999L;
        when(facturaRepository.findByPedidoId(pedidoIdInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facturaService.obtenerPorPedidoId(pedidoIdInvalido),
                "Debe lanzar ResourceNotFoundException cuando el pedido no tiene factura"
        );

        assertEquals("No existe un documento fiscal asociado al pedido ID: 999", exception.getMessage(),
                "El mensaje debe indicar el pedidoId para el que no se encontró documento fiscal");

        // VERIFY: findByPedidoId invocado 1 vez
        verify(facturaRepository, times(1)).findByPedidoId(pedidoIdInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al consultar la factura de un pedido no facturado la API devuelve HTTP 200 con cuerpo vacío:
         * - QA debe reportar: "Falla fiscal: El sistema responde OK sin datos para un pedido sin factura emitida."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en obtenerPorPedidoId()
         *   y si el repositorio tiene definido el método findByPedidoId().
         */
    }

    // =========================================================================
    // actualizarFactura()
    // =========================================================================

    @Test
    @DisplayName("actualizarFactura - Debe recalcular impuestos y total, y retornar el DTO actualizado")
    void actualizarFactura_DebeRecalcularValoresFiscalesYRetornarDTOActualizado_CuandoIdExiste() {
        // ARRANGE: Factura existente con valores anteriores
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Factura facturaExistente = Factura.builder()
                .id(id).pedidoId(10L).numeroFactura("FAC-ABCD1234")
                .subtotal(100.0).impuestos(19.0).total(119.0)
                .fechaEmision(ahora).estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        // Nuevo subtotal que llegará en el request (nota de crédito)
        double nuevoSubtotal = 200.0;
        double nuevosImpuestos = nuevoSubtotal * PORCENTAJE_IMPUESTO; // 38.0
        double nuevoTotal = nuevoSubtotal + nuevosImpuestos;           // 238.0

        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setPedidoId(10L);
        request.setSubtotal(nuevoSubtotal);

        // Factura que el repositorio retorna tras el save()
        Factura facturaActualizada = Factura.builder()
                .id(id).pedidoId(10L).numeroFactura("FAC-ABCD1234")
                .subtotal(nuevoSubtotal).impuestos(nuevosImpuestos).total(nuevoTotal)
                .fechaEmision(ahora).estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.findById(id)).thenReturn(Optional.of(facturaExistente));
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaActualizada);

        // ACT: Actualizar la factura
        FacturaResponseDTO resultado = facturaService.actualizarFactura(id, request);

        // ASSERT: Verificar que los nuevos valores fiscales se calcularon y mapearon correctamente
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId(), "El ID no debe cambiar");
        assertEquals(nuevoSubtotal, resultado.getSubtotal(), 0.001, "El subtotal debe haberse actualizado");
        assertEquals(nuevosImpuestos, resultado.getImpuestos(), 0.001, "Los impuestos deben recalcularse sobre el nuevo subtotal");
        assertEquals(nuevoTotal, resultado.getTotal(), 0.001, "El total debe ser subtotal + impuestos recalculados");
        assertEquals("EMITIDA", resultado.getEstadoFiscal(), "El estado fiscal no debe cambiar en una actualización de valores");
        assertEquals("FAC-ABCD1234", resultado.getNumeroFactura(), "El número de factura no debe cambiar");

        // VERIFY: findById + save invocados una vez cada uno
        verify(facturaRepository, times(1)).findById(id);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("actualizarFactura - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarFactura_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular factura inexistente
        Long idInvalido = 999L;
        FacturaRequestDTO request = new FacturaRequestDTO();
        request.setPedidoId(10L);
        request.setSubtotal(200.0);

        when(facturaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facturaService.actualizarFactura(idInvalido, request),
                "Debe lanzar ResourceNotFoundException cuando la factura no existe"
        );

        assertEquals("Factura no encontrada con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, save NO debe haberse invocado
        verify(facturaRepository, times(1)).findById(idInvalido);
        verify(facturaRepository, never()).save(any(Factura.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al actualizar una factura inexistente el sistema crea una nueva en lugar de responder 404:
         * - QA debe reportar: "Falla al actualizar factura: El sistema crea una factura nueva en lugar de responder 404."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está antes del save(), y que no se esté
         *   llamando a save() sin haber encontrado el registro previamente.
         */
    }

    // =========================================================================
    // cambiarEstadoFiscal()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstadoFiscal - Debe cambiar el estado tributario y retornar el DTO actualizado")
    void cambiarEstadoFiscal_DebeActualizarEstadoFiscal_CuandoIdExiste() {
        // ARRANGE: Factura existente con estado EMITIDA
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        String nuevoEstado = "ANULADA";

        Factura facturaExistente = Factura.builder()
                .id(id).pedidoId(10L).numeroFactura("FAC-ABCD1234")
                .subtotal(100.0).impuestos(19.0).total(119.0)
                .fechaEmision(ahora).estadoFiscal("EMITIDA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        // Factura que devuelve el repositorio tras el save() con nuevo estado
        Factura facturaConNuevoEstado = Factura.builder()
                .id(id).pedidoId(10L).numeroFactura("FAC-ABCD1234")
                .subtotal(100.0).impuestos(19.0).total(119.0)
                .fechaEmision(ahora).estadoFiscal(nuevoEstado)
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.findById(id)).thenReturn(Optional.of(facturaExistente));
        when(facturaRepository.save(any(Factura.class))).thenReturn(facturaConNuevoEstado);

        // ACT: Cambiar el estado fiscal
        FacturaResponseDTO resultado = facturaService.cambiarEstadoFiscal(id, nuevoEstado);

        // ASSERT: Solo debe cambiar el estadoFiscal, el resto de campos permanece igual
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId(), "El ID no debe cambiar");
        assertEquals("ANULADA", resultado.getEstadoFiscal(), "El estado fiscal debe haberse actualizado a ANULADA");
        assertEquals("FAC-ABCD1234", resultado.getNumeroFactura(), "El número de factura no debe cambiar");
        assertEquals(100.0, resultado.getSubtotal(), 0.001, "El subtotal no debe cambiar");
        assertEquals(119.0, resultado.getTotal(), 0.001, "El total no debe cambiar");

        // VERIFY: findById + save invocados una vez cada uno
        verify(facturaRepository, times(1)).findById(id);
        verify(facturaRepository, times(1)).save(any(Factura.class));
    }

    @Test
    @DisplayName("cambiarEstadoFiscal - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstadoFiscal_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular factura inexistente
        Long idInvalido = 999L;
        when(facturaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facturaService.cambiarEstadoFiscal(idInvalido, "ANULADA"),
                "Debe lanzar ResourceNotFoundException cuando la factura no existe"
        );

        assertEquals("Factura no encontrada con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, save NO debe haberse invocado
        verify(facturaRepository, times(1)).findById(idInvalido);
        verify(facturaRepository, never()).save(any(Factura.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar anular una factura inexistente el sistema responde HTTP 200 en lugar de 404:
         * - QA debe reportar: "Falla al anular factura: La API acepta la anulación de una factura inexistente sin error."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado antes de llamar a setEstadoFiscal() y save().
         */
    }

    // =========================================================================
    // eliminarFactura()
    // =========================================================================

    @Test
    @DisplayName("eliminarFactura - Debe eliminar el registro fiscal correctamente cuando el ID existe")
    void eliminarFactura_DebeEliminarFactura_CuandoIdExiste() {
        // ARRANGE: Factura existente lista para ser eliminada
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();

        Factura factura = Factura.builder()
                .id(id).pedidoId(10L).numeroFactura("FAC-ABCD1234")
                .subtotal(100.0).impuestos(19.0).total(119.0)
                .fechaEmision(ahora).estadoFiscal("ANULADA")
                .urlPdf("https://restaurante.com/facturas/FAC-ABCD1234.pdf")
                .build();

        when(facturaRepository.findById(id)).thenReturn(Optional.of(factura));
        doNothing().when(facturaRepository).delete(factura);

        // ACT: Eliminar la factura (void)
        assertDoesNotThrow(() -> facturaService.eliminarFactura(id),
                "No debe lanzar excepción al eliminar una factura existente");

        // VERIFY: findById + delete invocados una vez cada uno
        verify(facturaRepository, times(1)).findById(id);
        verify(facturaRepository, times(1)).delete(factura);
    }

    @Test
    @DisplayName("eliminarFactura - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarFactura_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular factura inexistente
        Long idInvalido = 999L;
        when(facturaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> facturaService.eliminarFactura(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando la factura a eliminar no existe"
        );

        assertEquals("Factura no encontrada con ID: 999", exception.getMessage());

        // VERIFY: findById se llamó, delete NO debe haberse invocado
        verify(facturaRepository, times(1)).findById(idInvalido);
        verify(facturaRepository, never()).delete(any(Factura.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /facturas/{id} responde HTTP 204 (sin contenido) para una factura que no existe:
         * - QA debe reportar: "Falla al eliminar factura: La API confirma eliminación de un registro inexistente."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está presente antes del delete(),
         *   y si el GlobalExceptionHandler está capturando y convirtiendo la excepción a HTTP 404.
         */
    }
}






package com.restaurante.ms_cocina.service;

import com.restaurante.ms_cocina.dto.ComandaRequestDTO;
import com.restaurante.ms_cocina.dto.ComandaResponseDTO;
import com.restaurante.ms_cocina.entity.Comanda;
import com.restaurante.ms_cocina.repository.ComandaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import com.restaurante.ms_cocina.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ComandaServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (ComandaRepository) con @Mock.
 * - Se inyectan los mocks en ComandaServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class ComandaServiceImplTest {

    @Mock
    private ComandaRepository comandaRepository;

    @InjectMocks
    private ComandaServiceImpl comandaService;

    // =========================================================================
    // crearComanda()
    // =========================================================================

    @Test
    @DisplayName("crearComanda - Debe guardar y retornar la comanda mapeada a DTO con estado PENDIENTE")
    void crearComanda_DebeRetornarComandaResponseDTO_CuandoRequestEsValido() {
        // ARRANGE: Preparar DTO de entrada, entidad simulada y su resultado al guardar
        ComandaRequestDTO request = new ComandaRequestDTO();
        request.setPedidoId(10L);
        request.setPlatoId(5L);
        request.setCantidad(2);
        request.setNotas("Sin cebolla");

        Comanda comandaGuardada = Comanda.builder()
                .id(1L)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        // Configurar Mockito para simular el comportamiento del repositorio
        when(comandaRepository.save(any(Comanda.class))).thenReturn(comandaGuardada);

        // ACT: Invocar el método del servicio a probar
        ComandaResponseDTO resultado = comandaService.crearComanda(request);

        // ASSERT: Verificar que la respuesta contenga los datos mapeados correctamente
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID mapeado no coincide");
        assertEquals(10L, resultado.getPedidoId(), "El ID del pedido no coincide");
        assertEquals(5L, resultado.getPlatoId(), "El ID del plato no coincide");
        assertEquals(2, resultado.getCantidad(), "La cantidad no coincide");
        assertEquals("PENDIENTE", resultado.getEstado(), "El estado inicial debe ser PENDIENTE");
        assertEquals("Sin cebolla", resultado.getNotas(), "Las notas no coinciden");

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(comandaRepository, times(1)).save(any(Comanda.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al crear la comanda se lanza un NullPointerException:
         * - QA debe reportar: "Falla al registrar comanda: NullPointerException en el flujo de negocio."
         * - Desarrollo debe revisar: Si el builder de la entidad Comanda o el mapeador mapToDTO está accediendo a campos nulos.
         */
    }

    // =========================================================================
    // obtenerTodas()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodas - Debe retornar la lista de comandas mapeada a DTO")
    void obtenerTodas_DebeRetornarListaDeComandasResponseDTO() {
        // ARRANGE: Preparar dos comandas de prueba en el repositorio simulado
        Comanda comanda1 = Comanda.builder()
                .id(1L)
                .pedidoId(10L)
                .platoId(3L)
                .cantidad(1)
                .estado("PENDIENTE")
                .notas("Sin sal")
                .build();

        Comanda comanda2 = Comanda.builder()
                .id(2L)
                .pedidoId(10L)
                .platoId(7L)
                .cantidad(2)
                .estado("EN_PREPARACION")
                .notas(null)
                .build();

        when(comandaRepository.findAll()).thenReturn(java.util.Arrays.asList(comanda1, comanda2));

        // ACT: Invocar el método del servicio a probar
        List<ComandaResponseDTO> resultado = comandaService.obtenerTodas();

        // ASSERT: Verificar tamaño de lista y valores mapeados correctamente
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El tamaño de la lista debe ser 2");

        ComandaResponseDTO dto1 = resultado.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals(10L, dto1.getPedidoId());
        assertEquals("PENDIENTE", dto1.getEstado());
        assertEquals("Sin sal", dto1.getNotas());

        ComandaResponseDTO dto2 = resultado.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("EN_PREPARACION", dto2.getEstado());
        assertNull(dto2.getNotas(), "Las notas deben ser nulas cuando no se especifican");

        // VERIFY: Comprobar que findAll() fue invocado una sola vez
        verify(comandaRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el servicio retorna una lista vacía aunque existan comandas en la base de datos:
         * - QA debe reportar: "Falla al listar comandas: La API retorna lista vacía cuando existen comandas activas."
         * - Desarrollo debe revisar: Si findAll() está siendo llamado correctamente o hay un error en el stream/map.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar la comanda mapeada a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarComandaResponseDTO_CuandoIdExiste() {
        // ARRANGE: Preparar entidad simulada y configurar repositorio
        Long idBusqueda = 1L;
        Comanda comanda = Comanda.builder()
                .id(idBusqueda)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Término medio")
                .build();

        when(comandaRepository.findById(idBusqueda)).thenReturn(Optional.of(comanda));

        // ACT: Invocar el método del servicio
        ComandaResponseDTO resultado = comandaService.obtenerPorId(idBusqueda);

        // ASSERT: Verificar que todos los atributos estén correctamente mapeados
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID no coincide");
        assertEquals(10L, resultado.getPedidoId(), "El pedidoId no coincide");
        assertEquals(5L, resultado.getPlatoId(), "El platoId no coincide");
        assertEquals(2, resultado.getCantidad(), "La cantidad no coincide");
        assertEquals("PENDIENTE", resultado.getEstado(), "El estado no coincide");
        assertEquals("Término medio", resultado.getNotas(), "Las notas no coinciden");

        // VERIFY: Comprobar que findById fue invocado con el ID correcto
        verify(comandaRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular ausencia del recurso en la base de datos
        Long idInvalido = 999L;
        when(comandaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> comandaService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando el ID no existe"
        );

        assertEquals("Comanda no encontrada con ID: 999", exception.getMessage(),
                "El mensaje de la excepción debe indicar el ID buscado");

        // VERIFY
        verify(comandaRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar un ID inexistente se obtiene HTTP 500 en lugar de 404:
         * - QA debe reportar: "Falla al buscar comanda: La API responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el controlador tiene un @ExceptionHandler para ResourceNotFoundException.
         */
    }

    // =========================================================================
    // obtenerPorPedidoId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorPedidoId - Debe retornar solo las comandas del pedido solicitado")
    void obtenerPorPedidoId_DebeRetornarListaFiltradaPorPedido() {
        // ARRANGE: Preparar comandas que pertenecen al mismo pedido
        Long pedidoId = 10L;

        Comanda comanda1 = Comanda.builder()
                .id(1L)
                .pedidoId(pedidoId)
                .platoId(3L)
                .cantidad(1)
                .estado("PENDIENTE")
                .notas("Sin sal")
                .build();

        Comanda comanda2 = Comanda.builder()
                .id(2L)
                .pedidoId(pedidoId)
                .platoId(7L)
                .cantidad(2)
                .estado("EN_PREPARACION")
                .notas(null)
                .build();

        when(comandaRepository.findByPedidoId(pedidoId))
                .thenReturn(java.util.Arrays.asList(comanda1, comanda2));

        // ACT: Consultar comandas por pedido
        List<ComandaResponseDTO> resultado = comandaService.obtenerPorPedidoId(pedidoId);

        // ASSERT: Verificar que la lista tenga solo las comandas del pedido indicado
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size(), "Deben retornarse exactamente 2 comandas para este pedido");

        assertEquals(1L, resultado.get(0).getId());
        assertEquals(pedidoId, resultado.get(0).getPedidoId(), "La comanda debe pertenecer al pedido 10");
        assertEquals("PENDIENTE", resultado.get(0).getEstado());

        assertEquals(2L, resultado.get(1).getId());
        assertEquals(pedidoId, resultado.get(1).getPedidoId(), "La comanda debe pertenecer al pedido 10");
        assertEquals("EN_PREPARACION", resultado.get(1).getEstado());

        // VERIFY: Comprobar que findByPedidoId fue invocado con el ID correcto
        verify(comandaRepository, times(1)).findByPedidoId(pedidoId);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la consulta retorna comandas de otros pedidos mezcladas:
         * - QA debe reportar: "Falla en filtro por pedido: La API retorna comandas de otros pedidos junto con las del pedido solicitado."
         * - Desarrollo debe revisar: Si el método findByPedidoId del repositorio está correctamente definido
         *   o si hay algún error en la query derivada de Spring Data JPA.
         */
    }

    // =========================================================================
    // actualizarComanda()
    // =========================================================================

    @Test
    @DisplayName("actualizarComanda - Debe actualizar los campos y retornar el DTO actualizado")
    void actualizarComanda_DebeRetornarComandaActualizada_CuandoIdExiste() {
        // ARRANGE: Comanda existente en la base de datos
        Long id = 1L;

        Comanda comandaExistente = Comanda.builder()
                .id(id)
                .pedidoId(10L)
                .platoId(3L)
                .cantidad(1)
                .estado("PENDIENTE")
                .notas("Sin sal")
                .build();

        // Request con los nuevos datos
        ComandaRequestDTO request = new ComandaRequestDTO();
        request.setPedidoId(10L);
        request.setPlatoId(7L);
        request.setCantidad(3);
        request.setNotas("Con extra queso");

        // La comanda que el repositorio devuelve tras el save()
        Comanda comandaActualizada = Comanda.builder()
                .id(id)
                .pedidoId(10L)
                .platoId(7L)
                .cantidad(3)
                .estado("PENDIENTE")
                .notas("Con extra queso")
                .build();

        when(comandaRepository.findById(id)).thenReturn(Optional.of(comandaExistente));
        when(comandaRepository.save(any(Comanda.class))).thenReturn(comandaActualizada);

        // ACT: Actualizar la comanda
        ComandaResponseDTO resultado = comandaService.actualizarComanda(id, request);

        // ASSERT: Verificar que los campos reflejan los nuevos valores
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId(), "El ID no debe cambiar tras la actualización");
        assertEquals(7L, resultado.getPlatoId(), "El platoId debe haberse actualizado");
        assertEquals(3, resultado.getCantidad(), "La cantidad debe haberse actualizado");
        assertEquals("Con extra queso", resultado.getNotas(), "Las notas deben haberse actualizado");
        assertEquals("PENDIENTE", resultado.getEstado(), "El estado no debe cambiar en una actualización de datos");

        // VERIFY: findById + save deben haberse invocado una vez cada uno
        verify(comandaRepository, times(1)).findById(id);
        verify(comandaRepository, times(1)).save(any(Comanda.class));
    }

    @Test
    @DisplayName("actualizarComanda - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarComanda_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular que la comanda no existe
        Long idInvalido = 999L;
        ComandaRequestDTO request = new ComandaRequestDTO();
        request.setPedidoId(10L);
        request.setPlatoId(5L);
        request.setCantidad(2);
        request.setNotas("Notas de prueba");

        when(comandaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> comandaService.actualizarComanda(idInvalido, request),
                "Debe lanzar ResourceNotFoundException cuando la comanda no existe"
        );

        assertEquals("Comanda no encontrada con ID: 999", exception.getMessage(),
                "El mensaje de la excepción debe indicar el ID buscado");

        // VERIFY: findById se invocó, save NO debe haberse llamado
        verify(comandaRepository, times(1)).findById(idInvalido);
        verify(comandaRepository, never()).save(any(Comanda.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar actualizar una comanda inexistente el sistema devuelve HTTP 200 con datos vacíos:
         * - QA debe reportar: "Falla al actualizar comanda inexistente: La API responde 200 en lugar de 404."
         * - Desarrollo debe revisar: Si el orElseThrow() está correctamente implementado en el servicio.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe cambiar el estado de la comanda y retornar el DTO actualizado")
    void cambiarEstado_DebeActualizarEstado_CuandoIdExiste() {
        // ARRANGE: Comanda existente con estado inicial PENDIENTE
        Long id = 1L;
        String nuevoEstado = "EN_PREPARACION";

        Comanda comandaExistente = Comanda.builder()
                .id(id)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        // La comanda que devuelve el repositorio tras el save() con el nuevo estado
        Comanda comandaConNuevoEstado = Comanda.builder()
                .id(id)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado(nuevoEstado)
                .notas("Sin cebolla")
                .build();

        when(comandaRepository.findById(id)).thenReturn(Optional.of(comandaExistente));
        when(comandaRepository.save(any(Comanda.class))).thenReturn(comandaConNuevoEstado);

        // ACT: Cambiar el estado de la comanda
        ComandaResponseDTO resultado = comandaService.cambiarEstado(id, nuevoEstado);

        // ASSERT: Verificar que el estado fue actualizado y el resto de campos permanece igual
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(id, resultado.getId(), "El ID no debe cambiar");
        assertEquals("EN_PREPARACION", resultado.getEstado(),
                "El estado debe haberse actualizado a EN_PREPARACION");
        assertEquals(10L, resultado.getPedidoId(), "El pedidoId no debe cambiar");
        assertEquals(5L, resultado.getPlatoId(), "El platoId no debe cambiar");
        assertEquals(2, resultado.getCantidad(), "La cantidad no debe cambiar");
        assertEquals("Sin cebolla", resultado.getNotas(), "Las notas no deben cambiar");

        // VERIFY: findById + save invocados una vez cada uno
        verify(comandaRepository, times(1)).findById(id);
        verify(comandaRepository, times(1)).save(any(Comanda.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular que la comanda no existe en la base de datos
        Long idInvalido = 999L;
        when(comandaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> comandaService.cambiarEstado(idInvalido, "LISTO"),
                "Debe lanzar ResourceNotFoundException cuando la comanda no existe"
        );

        assertEquals("Comanda no encontrada con ID: 999", exception.getMessage(),
                "El mensaje debe indicar el ID que no fue encontrado");

        // VERIFY: findById se llamó, save NO debe haberse invocado
        verify(comandaRepository, times(1)).findById(idInvalido);
        verify(comandaRepository, never()).save(any(Comanda.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar cambiar el estado de una comanda inexistente la API devuelve HTTP 200:
         * - QA debe reportar: "Falla al cambiar estado: La comanda con ID inválido acepta cambio de estado sin error."
         * - Desarrollo debe revisar: Si el orElseThrow() está presente y el @ExceptionHandler maneja correctamente
         *   la ResourceNotFoundException en el GlobalExceptionHandler.
         */
    }

    // =========================================================================
    // eliminarComanda()
    // =========================================================================

    @Test
    @DisplayName("eliminarComanda - Debe eliminar la comanda cuando el ID existe")
    void eliminarComanda_DebeEliminarComanda_CuandoIdExiste() {
        // ARRANGE: Comanda existente
        Long id = 1L;
        Comanda comanda = Comanda.builder()
                .id(id)
                .pedidoId(10L)
                .platoId(5L)
                .cantidad(2)
                .estado("PENDIENTE")
                .notas("Sin cebolla")
                .build();

        when(comandaRepository.findById(id)).thenReturn(Optional.of(comanda));
        doNothing().when(comandaRepository).delete(comanda);

        // ACT: Llamar a eliminar
        assertDoesNotThrow(() -> comandaService.eliminarComanda(id), 
                "No debe lanzar ninguna excepción al eliminar una comanda existente");

        // VERIFY: Verificar llamadas
        verify(comandaRepository, times(1)).findById(id);
        verify(comandaRepository, times(1)).delete(comanda);
    }

    @Test
    @DisplayName("eliminarComanda - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarComanda_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Simular que el ID no existe
        Long idInvalido = 999L;
        when(comandaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar excepción
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> comandaService.eliminarComanda(idInvalido),
                "Debe lanzar ResourceNotFoundException cuando el ID no existe"
        );

        assertEquals("Comanda no encontrada con ID: 999", exception.getMessage(),
                "El mensaje debe indicar el ID que no fue encontrado");

        // VERIFY: findById se llamó, delete NO debe haberse invocado
        verify(comandaRepository, times(1)).findById(idInvalido);
        verify(comandaRepository, never()).delete(any(Comanda.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al intentar eliminar una comanda inexistente el sistema devuelve HTTP 200 en lugar de 404:
         * - QA debe reportar: "Falla al eliminar comanda: La API permite eliminar o responde OK al eliminar comanda que no existe."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en el flujo de negocio antes del borrado.
         */
    }
}


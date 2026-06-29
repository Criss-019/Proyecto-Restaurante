package com.restaurante.ms_reservas.service;

import com.restaurante.ms_reservas.dto.ReservaRequestDTO;
import com.restaurante.ms_reservas.dto.ReservaResponseDTO;
import com.restaurante.ms_reservas.entity.Reserva;
import com.restaurante.ms_reservas.exception.ResourceNotFoundException;
import com.restaurante.ms_reservas.repository.ReservaRepository;
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
 * Pruebas unitarias para ReservaServiceImpl.
 *
 * Estrategia:
 * - MockitoExtension para pruebas unitarias puras y rápidas.
 * - Simular la base de datos (ReservaRepository).
 * - Estructura: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceImplTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaServiceImpl reservaService;

    // =========================================================================
    // crearReserva()
    // =========================================================================

    @Test
    @DisplayName("crearReserva - Debe registrar la reserva localmente en estado CONFIRMADA")
    void crearReserva_DebeRetornarReservaResponseDTO_CuandoRequestEsValido() {
        // ARRANGE
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2);
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setClienteId(10L);
        request.setFechaHora(fechaHora);
        request.setCantidadPersonas(4);
        request.setObservaciones("Mesa cerca de la ventana");

        Reserva reservaGuardada = Reserva.builder()
                .id(1L)
                .clienteId(10L)
                .fechaHora(fechaHora)
                .cantidadPersonas(4)
                .estado("CONFIRMADA")
                .observaciones("Mesa cerca de la ventana")
                .build();

        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaGuardada);

        // ACT
        ReservaResponseDTO resultado = reservaService.crearReserva(request);

        // ASSERT
        assertNotNull(resultado, "La reserva retornada no debe ser nula");
        assertEquals(1L, resultado.getId());
        assertEquals(10L, resultado.getClienteId());
        assertEquals(fechaHora, resultado.getFechaHora());
        assertEquals(4, resultado.getCantidadPersonas());
        assertEquals("CONFIRMADA", resultado.getEstado(), "El estado inicial por defecto debe ser CONFIRMADA");
        assertEquals("Mesa cerca de la ventana", resultado.getObservaciones());

        // VERIFY
        verify(reservaRepository, times(1)).save(any(Reserva.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la reserva se registra con un estado diferente a CONFIRMADA (ej: PENDIENTE):
         * - QA debe reportar: "Falla al crear reserva: La reserva no se crea en estado CONFIRMADA por defecto."
         * - Desarrollo debe revisar: Si el builder de la entidad Reserva está configurando correctamente el estado como 'CONFIRMADA'.
         */
    }

    // =========================================================================
    // obtenerTodas()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodas - Debe retornar una lista de todas las reservas mapeadas a DTO")
    void obtenerTodas_DebeRetornarListaDeReservaResponseDTO() {
        // ARRANGE
        LocalDateTime ahora = LocalDateTime.now();
        Reserva r1 = Reserva.builder()
                .id(1L).clienteId(10L).fechaHora(ahora).cantidadPersonas(2)
                .estado("CONFIRMADA").observaciones("Sin observaciones").build();

        Reserva r2 = Reserva.builder()
                .id(2L).clienteId(11L).fechaHora(ahora.plusHours(2)).cantidadPersonas(5)
                .estado("CANCELADA").observaciones("Observaciones varias").build();

        when(reservaRepository.findAll()).thenReturn(java.util.Arrays.asList(r1, r2));

        // ACT
        java.util.List<ReservaResponseDTO> resultado = reservaService.obtenerTodas();

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size());

        assertEquals(1L, resultado.get(0).getId());
        assertEquals("CONFIRMADA", resultado.get(0).getEstado());
        assertEquals(2, resultado.get(0).getCantidadPersonas());

        assertEquals(2L, resultado.get(1).getId());
        assertEquals("CANCELADA", resultado.get(1).getEstado());
        assertEquals(5, resultado.get(1).getCantidadPersonas());

        // VERIFY
        verify(reservaRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el listado de reservas entrega una lista vacía habiendo reservas guardadas:
         * - QA debe reportar: "Falla al listar reservas: La respuesta de la API no contiene registros."
         * - Desarrollo debe revisar: Si la llamada al repositorio o el mapeo stream/mapToDTO están retornando una lista sin elementos.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar la reserva mapeada a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarReservaResponseDTO_CuandoIdExiste() {
        // ARRANGE
        Long idBusqueda = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Reserva reserva = Reserva.builder()
                .id(idBusqueda).clienteId(10L).fechaHora(ahora).cantidadPersonas(2)
                .estado("CONFIRMADA").observaciones("Mesa terraza").build();

        when(reservaRepository.findById(idBusqueda)).thenReturn(Optional.of(reserva));

        // ACT
        ReservaResponseDTO resultado = reservaService.obtenerPorId(idBusqueda);

        // ASSERT
        assertNotNull(resultado, "La respuesta no debe ser nula");
        assertEquals(idBusqueda, resultado.getId());
        assertEquals(10L, resultado.getClienteId());
        assertEquals("CONFIRMADA", resultado.getEstado());
        assertEquals("Mesa terraza", resultado.getObservaciones());

        // VERIFY
        verify(reservaRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(reservaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException si la reserva no existe"
        );

        assertEquals("Reserva no encontrada con ID: 999", exception.getMessage());

        // VERIFY
        verify(reservaRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar una reserva por ID inexistente la API devuelve HTTP 500 en lugar de 404:
         * - QA debe reportar: "Falla al buscar reserva: Responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el GlobalExceptionHandler captura adecuadamente la excepción ResourceNotFoundException.
         */
    }

    // =========================================================================
    // actualizarReserva()
    // =========================================================================

    @Test
    @DisplayName("actualizarReserva - Debe actualizar los datos de la reserva y retornar el DTO actualizado")
    void actualizarReserva_DebeActualizarCampos_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        LocalDateTime fechaOriginal = LocalDateTime.now().plusDays(1);
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(2);

        Reserva reservaExistente = Reserva.builder()
                .id(id).clienteId(10L).fechaHora(fechaOriginal).cantidadPersonas(2)
                .estado("CONFIRMADA").observaciones("Mesa terraza").build();

        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setClienteId(12L);
        request.setFechaHora(nuevaFecha);
        request.setCantidadPersonas(4);
        request.setObservaciones("Mesa adentro, salón VIP");

        Reserva reservaActualizada = Reserva.builder()
                .id(id).clienteId(12L).fechaHora(nuevaFecha).cantidadPersonas(4)
                .estado("CONFIRMADA").observaciones("Mesa adentro, salón VIP").build();

        when(reservaRepository.findById(id)).thenReturn(Optional.of(reservaExistente));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaActualizada);

        // ACT
        ReservaResponseDTO resultado = reservaService.actualizarReserva(id, request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(12L, resultado.getClienteId());
        assertEquals(nuevaFecha, resultado.getFechaHora());
        assertEquals(4, resultado.getCantidadPersonas());
        assertEquals("Mesa adentro, salón VIP", resultado.getObservaciones());
        assertEquals("CONFIRMADA", resultado.getEstado(), "El estado no debe cambiar al actualizar los datos base");

        // VERIFY
        verify(reservaRepository, times(1)).findById(id);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    @DisplayName("actualizarReserva - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizarReserva_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        ReservaRequestDTO request = new ReservaRequestDTO();
        request.setClienteId(12L);
        request.setFechaHora(LocalDateTime.now().plusDays(2));
        request.setCantidadPersonas(4);
        request.setObservaciones("VIP");

        when(reservaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaService.actualizarReserva(idInvalido, request),
                "Debe lanzar ResourceNotFoundException si la reserva no existe"
        );

        assertEquals("Reserva no encontrada con ID: 999", exception.getMessage());

        // VERIFY: no llama a save
        verify(reservaRepository, times(1)).findById(idInvalido);
        verify(reservaRepository, never()).save(any(Reserva.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API crea una nueva reserva al intentar actualizar un ID inexistente:
         * - QA debe reportar: "Falla al actualizar: Se crea un nuevo registro de reserva en la base de datos en lugar de error 404."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio antes del save.
         */
    }

    // =========================================================================
    // cambiarEstado()
    // =========================================================================

    @Test
    @DisplayName("cambiarEstado - Debe actualizar el estado de la reserva y retornar el DTO actualizado")
    void cambiarEstado_DebeActualizarEstado_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        String nuevoEstado = "CANCELADA";
        LocalDateTime ahora = LocalDateTime.now();
        Reserva reservaExistente = Reserva.builder()
                .id(id).clienteId(10L).fechaHora(ahora).cantidadPersonas(2)
                .estado("CONFIRMADA").observaciones("Terraza").build();

        Reserva reservaActualizada = Reserva.builder()
                .id(id).clienteId(10L).fechaHora(ahora).cantidadPersonas(2)
                .estado(nuevoEstado).observaciones("Terraza").build();

        when(reservaRepository.findById(id)).thenReturn(Optional.of(reservaExistente));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaActualizada);

        // ACT
        ReservaResponseDTO resultado = reservaService.cambiarEstado(id, nuevoEstado);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals(nuevoEstado, resultado.getEstado(), "El estado debe haber cambiado a CANCELADA");
        assertEquals(10L, resultado.getClienteId());
        assertEquals("Terraza", resultado.getObservaciones());

        // VERIFY
        verify(reservaRepository, times(1)).findById(id);
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    @DisplayName("cambiarEstado - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void cambiarEstado_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(reservaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaService.cambiarEstado(idInvalido, "ASISTIDA"),
                "Debe lanzar ResourceNotFoundException si la reserva no existe"
        );

        assertEquals("Reserva no encontrada con ID: 999", exception.getMessage());

        // VERIFY: no llama a save
        verify(reservaRepository, times(1)).findById(idInvalido);
        verify(reservaRepository, never()).save(any(Reserva.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API responde con HTTP 200 en lugar de 404 al cambiar el estado de una reserva inexistente:
         * - QA debe reportar: "Falla al cambiar estado de reserva: Se reporta éxito al actualizar una reserva inexistente."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado en la lógica del servicio antes del save.
         */
    }

    // =========================================================================
    // eliminarReserva()
    // =========================================================================

    @Test
    @DisplayName("eliminarReserva - Debe eliminar la reserva correctamente cuando el ID existe")
    void eliminarReserva_DebeEliminarReserva_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        LocalDateTime ahora = LocalDateTime.now();
        Reserva reserva = Reserva.builder()
                .id(id).clienteId(10L).fechaHora(ahora).cantidadPersonas(2)
                .estado("CONFIRMADA").observaciones("Terraza").build();

        when(reservaRepository.findById(id)).thenReturn(Optional.of(reserva));
        doNothing().when(reservaRepository).delete(reserva);

        // ACT & ASSERT
        assertDoesNotThrow(() -> reservaService.eliminarReserva(id),
                "No debe lanzar excepción al eliminar una reserva existente");

        // VERIFY
        verify(reservaRepository, times(1)).findById(id);
        verify(reservaRepository, times(1)).delete(reserva);
    }

    @Test
    @DisplayName("eliminarReserva - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminarReserva_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(reservaRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservaService.eliminarReserva(idInvalido),
                "Debe lanzar ResourceNotFoundException si la reserva a eliminar no existe"
        );

        assertEquals("Reserva no encontrada con ID: 999", exception.getMessage());

        // VERIFY
        verify(reservaRepository, times(1)).findById(idInvalido);
        verify(reservaRepository, never()).delete(any(Reserva.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /reservas/{id} responde HTTP 204 (sin contenido) para una reserva inexistente:
         * - QA debe reportar: "Falla al eliminar reserva: La API confirma la eliminación de una reserva inexistente."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está implementado en la lógica del servicio antes del delete.
         */
    }
}

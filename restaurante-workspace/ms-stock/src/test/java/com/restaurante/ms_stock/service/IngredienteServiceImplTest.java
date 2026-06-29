package com.restaurante.ms_stock.service;

import com.restaurante.ms_stock.dto.IngredienteRequestDTO;
import com.restaurante.ms_stock.dto.IngredienteResponseDTO;
import com.restaurante.ms_stock.entity.Ingrediente;
import com.restaurante.ms_stock.exception.ResourceNotFoundException;
import com.restaurante.ms_stock.repository.IngredienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para IngredienteServiceImpl.
 *
 * Estrategia:
 * - MockitoExtension para pruebas unitarias puras y rápidas.
 * - Simular la base de datos (IngredienteRepository).
 * - Estructura: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class IngredienteServiceImplTest {

    @Mock
    private IngredienteRepository repository;

    @InjectMocks
    private IngredienteServiceImpl ingredienteService;

    // =========================================================================
    // crear()
    // =========================================================================

    @Test
    @DisplayName("crear - Debe registrar el ingrediente y retornar el DTO con todos los campos")
    void crear_DebeRetornarIngredienteResponseDTO_CuandoRequestEsValido() {
        // ARRANGE
        IngredienteRequestDTO request = new IngredienteRequestDTO();
        request.setNombre("Harina de trigo");
        request.setCantidadActual(50.0);
        request.setCantidadMinima(10.0);
        request.setUnidadMedida("kg");

        Ingrediente guardado = Ingrediente.builder()
                .id(1L)
                .nombre("Harina de trigo")
                .cantidadActual(50.0)
                .cantidadMinima(10.0)
                .unidadMedida("kg")
                .build();

        when(repository.save(any(Ingrediente.class))).thenReturn(guardado);

        // ACT
        IngredienteResponseDTO resultado = ingredienteService.crear(request);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(1L, resultado.getId());
        assertEquals("Harina de trigo", resultado.getNombre());
        assertEquals(50.0, resultado.getCantidadActual());
        assertEquals(10.0, resultado.getCantidadMinima());
        assertEquals("kg", resultado.getUnidadMedida());

        // VERIFY
        verify(repository, times(1)).save(any(Ingrediente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el ingrediente se registra sin cantidad mínima (null o 0):
         * - QA debe reportar: "Falla al crear ingrediente: La cantidad mínima no se persiste correctamente."
         * - Desarrollo debe revisar: Si el builder de la entidad Ingrediente mapea correctamente el campo cantidadMinima del DTO.
         */
    }

    // =========================================================================
    // listar()
    // =========================================================================

    @Test
    @DisplayName("listar - Debe retornar una lista de todos los ingredientes mapeados a DTO")
    void listar_DebeRetornarListaDeIngredienteResponseDTO() {
        // ARRANGE
        Ingrediente i1 = Ingrediente.builder()
                .id(1L).nombre("Harina").cantidadActual(50.0).cantidadMinima(10.0).unidadMedida("kg").build();
        Ingrediente i2 = Ingrediente.builder()
                .id(2L).nombre("Aceite").cantidadActual(20.0).cantidadMinima(5.0).unidadMedida("litros").build();

        when(repository.findAll()).thenReturn(Arrays.asList(i1, i2));

        // ACT
        List<IngredienteResponseDTO> resultado = ingredienteService.listar();

        // ASSERT
        assertNotNull(resultado, "La lista no debe ser nula");
        assertEquals(2, resultado.size());

        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Harina", resultado.get(0).getNombre());
        assertEquals(50.0, resultado.get(0).getCantidadActual());
        assertEquals("kg", resultado.get(0).getUnidadMedida());

        assertEquals(2L, resultado.get(1).getId());
        assertEquals("Aceite", resultado.get(1).getNombre());
        assertEquals(20.0, resultado.get(1).getCantidadActual());
        assertEquals("litros", resultado.get(1).getUnidadMedida());

        // VERIFY
        verify(repository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el listado de ingredientes devuelve una lista vacía habiendo registros en base de datos:
         * - QA debe reportar: "Falla al listar ingredientes: La respuesta de la API no contiene registros."
         * - Desarrollo debe revisar: Si el stream().map(this::mapToDTO).collect() está funcionando correctamente.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el DTO del ingrediente cuando el ID existe")
    void obtenerPorId_DebeRetornarIngredienteResponseDTO_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        Ingrediente ingrediente = Ingrediente.builder()
                .id(id).nombre("Sal").cantidadActual(15.0).cantidadMinima(2.0).unidadMedida("kg").build();

        when(repository.findById(id)).thenReturn(Optional.of(ingrediente));

        // ACT
        IngredienteResponseDTO resultado = ingredienteService.obtenerPorId(id);

        // ASSERT
        assertNotNull(resultado, "El resultado no debe ser nulo");
        assertEquals(id, resultado.getId());
        assertEquals("Sal", resultado.getNombre());
        assertEquals(15.0, resultado.getCantidadActual());
        assertEquals(2.0, resultado.getCantidadMinima());
        assertEquals("kg", resultado.getUnidadMedida());

        // VERIFY
        verify(repository, times(1)).findById(id);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(repository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ingredienteService.obtenerPorId(idInvalido),
                "Debe lanzar ResourceNotFoundException si el ingrediente no existe"
        );

        assertEquals("Ingrediente no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(repository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API devuelve HTTP 500 en lugar de 404 al buscar un ingrediente inexistente:
         * - QA debe reportar: "Falla al buscar ingrediente: La API responde 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el GlobalExceptionHandler captura correctamente ResourceNotFoundException.
         */
    }

    // =========================================================================
    // actualizar()
    // =========================================================================

    @Test
    @DisplayName("actualizar - Debe actualizar todos los campos del ingrediente y retornar el DTO actualizado")
    void actualizar_DebeActualizarCampos_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        Ingrediente ingredienteExistente = Ingrediente.builder()
                .id(id).nombre("Sal").cantidadActual(15.0).cantidadMinima(2.0).unidadMedida("kg").build();

        IngredienteRequestDTO request = new IngredienteRequestDTO();
        request.setNombre("Sal fina");
        request.setCantidadActual(30.0);
        request.setCantidadMinima(5.0);
        request.setUnidadMedida("g");

        Ingrediente ingredienteActualizado = Ingrediente.builder()
                .id(id).nombre("Sal fina").cantidadActual(30.0).cantidadMinima(5.0).unidadMedida("g").build();

        when(repository.findById(id)).thenReturn(Optional.of(ingredienteExistente));
        when(repository.save(any(Ingrediente.class))).thenReturn(ingredienteActualizado);

        // ACT
        IngredienteResponseDTO resultado = ingredienteService.actualizar(id, request);

        // ASSERT
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
        assertEquals("Sal fina", resultado.getNombre());
        assertEquals(30.0, resultado.getCantidadActual());
        assertEquals(5.0, resultado.getCantidadMinima());
        assertEquals("g", resultado.getUnidadMedida());

        // VERIFY
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(Ingrediente.class));
    }

    @Test
    @DisplayName("actualizar - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void actualizar_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        IngredienteRequestDTO request = new IngredienteRequestDTO();
        request.setNombre("Pimienta");
        request.setCantidadActual(10.0);
        request.setCantidadMinima(1.0);
        request.setUnidadMedida("kg");

        when(repository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ingredienteService.actualizar(idInvalido, request),
                "Debe lanzar ResourceNotFoundException si el ingrediente a actualizar no existe"
        );

        assertEquals("Ingrediente no encontrado con ID: 999", exception.getMessage());

        // VERIFY: no llama a save
        verify(repository, times(1)).findById(idInvalido);
        verify(repository, never()).save(any(Ingrediente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si la API crea un nuevo ingrediente al intentar actualizar un ID inexistente:
         * - QA debe reportar: "Falla al actualizar ingrediente: Se crea un nuevo registro en lugar de retornar 404."
         * - Desarrollo debe revisar: Si el orElseThrow() está implementado antes de la lógica de actualización.
         */
    }

    // =========================================================================
    // eliminar()
    // =========================================================================

    @Test
    @DisplayName("eliminar - Debe eliminar el ingrediente correctamente cuando el ID existe")
    void eliminar_DebeEliminarIngrediente_CuandoIdExiste() {
        // ARRANGE
        Long id = 1L;
        Ingrediente ingrediente = Ingrediente.builder()
                .id(id).nombre("Azúcar").cantidadActual(25.0).cantidadMinima(5.0).unidadMedida("kg").build();

        when(repository.findById(id)).thenReturn(Optional.of(ingrediente));
        doNothing().when(repository).delete(ingrediente);

        // ACT & ASSERT
        assertDoesNotThrow(() -> ingredienteService.eliminar(id),
                "No debe lanzar excepción al eliminar un ingrediente existente");

        // VERIFY
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).delete(ingrediente);
    }

    @Test
    @DisplayName("eliminar - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void eliminar_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE
        Long idInvalido = 999L;
        when(repository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ingredienteService.eliminar(idInvalido),
                "Debe lanzar ResourceNotFoundException si el ingrediente a eliminar no existe"
        );

        assertEquals("Ingrediente no encontrado con ID: 999", exception.getMessage());

        // VERIFY
        verify(repository, times(1)).findById(idInvalido);
        verify(repository, never()).delete(any(Ingrediente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el endpoint DELETE /ingredientes/{id} responde HTTP 204 para un ID inexistente:
         * - QA debe reportar: "Falla al eliminar ingrediente: La API confirma la eliminación de un ingrediente que no existe."
         * - Desarrollo debe revisar: Si el findById().orElseThrow() está implementado en la lógica del servicio antes del delete.
         */
    }
}

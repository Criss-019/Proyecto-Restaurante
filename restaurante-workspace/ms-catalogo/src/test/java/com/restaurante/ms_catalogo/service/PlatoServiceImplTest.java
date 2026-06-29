package com.restaurante.ms_catalogo.service;

import com.restaurante.ms_catalogo.dto.PlatoRequestDTO;
import com.restaurante.ms_catalogo.dto.PlatoResponseDTO;
import com.restaurante.ms_catalogo.entity.Plato;
import com.restaurante.ms_catalogo.repository.PlatoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import com.restaurante.ms_catalogo.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para PlatoServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (PlatoRepository) con @Mock.
 * - Se inyectan los mocks en PlatoServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class PlatoServiceImplTest {

    @Mock
    private PlatoRepository platoRepository;

    @InjectMocks
    private PlatoServiceImpl platoService;

    // =========================================================================
    // crearPlato()
    // =========================================================================

    @Test
    @DisplayName("crearPlato - Debe guardar y retornar el plato mapeado a DTO")
    void crearPlato_DebeRetornarPlatoResponseDTO_CuandoRequestEsValido() {
        // ARRANGE: Preparar DTO de entrada, entidad simulada y su resultado al guardar
        PlatoRequestDTO request = new PlatoRequestDTO();
        request.setNombre("Tallarines Pesto");
        request.setDescripcion("Tallarines con salsa pesto tradicional");
        request.setPrecio(12.50);
        request.setDisponible(true);

        Plato platoGuardado = Plato.builder()
                .id(10L)
                .nombre("Tallarines Pesto")
                .descripcion("Tallarines con salsa pesto tradicional")
                .precio(12.50)
                .disponible(true)
                .build();

        // Configurar Mockito para simular el comportamiento del repositorio
        when(platoRepository.save(any(Plato.class))).thenReturn(platoGuardado);

        // ACT: Invocar el método del servicio a probar
        PlatoResponseDTO resultado = platoService.crearPlato(request);

        // ASSERT: Verificar que la respuesta contenga los datos mapeados correctamente
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(10L, resultado.getId(), "El ID mapeado no coincide");
        assertEquals("Tallarines Pesto", resultado.getNombre(), "El nombre no coincide");
        assertEquals("Tallarines Pesto", resultado.getNombre());
        assertEquals("Tallarines con salsa pesto tradicional", resultado.getDescripcion());
        assertEquals(12.50, resultado.getPrecio());
        assertTrue(resultado.getDisponible());

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(platoRepository, times(1)).save(any(Plato.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al crear el plato se lanza un NullPointerException:
         * - QA debe reportar: "Falla al registrar plato: NullPointerException en el flujo de negocio."
         * - Desarrollo debe revisar: Si el constructor/builder de la entidad Plato o el mapeador mapToDTO está accediendo a variables nulas.
         */
    }

    // =========================================================================
    // obtenerTodos()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodos - Debe retornar la lista de platos mapeada a DTO")
    void obtenerTodos_DebeRetornarListaDePlatosResponseDTO() {
        // ARRANGE: Preparar platos de prueba en el repositorio simulado
        Plato plato1 = Plato.builder()
                .id(1L)
                .nombre("Lomo Saltado")
                .descripcion("Cortes de lomo salteado con cebolla y tomate")
                .precio(15.90)
                .disponible(true)
                .build();

        Plato plato2 = Plato.builder()
                .id(2L)
                .nombre("Ceviche")
                .descripcion("Pescado marinado en jugo de limón")
                .precio(18.50)
                .disponible(true)
                .build();

        when(platoRepository.findAll()).thenReturn(java.util.Arrays.asList(plato1, plato2));

        // ACT: Invocar el método del servicio a probar
        List<PlatoResponseDTO> resultado = platoService.obtenerTodos();

        // ASSERT: Verificar que la lista retorne los platos esperados mapeados
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El tamaño de la lista debe ser 2");

        PlatoResponseDTO dto1 = resultado.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Lomo Saltado", dto1.getNombre());
        assertEquals(15.90, dto1.getPrecio());

        PlatoResponseDTO dto2 = resultado.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Ceviche", dto2.getNombre());
        assertEquals(18.50, dto2.getPrecio());

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(platoRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el servicio retorna una lista vacía habiendo datos guardados:
         * - QA debe reportar: "Falla al listar platos: La API retorna una lista vacía cuando existen platos en el catálogo."
         * - Desarrollo debe revisar: Si se está llamando correctamente al findAll() del repositorio o si hay un error en el filter/stream.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el plato mapeado a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarPlatoResponseDTO_CuandoIdExiste() {
        // ARRANGE: Preparar entidad simulada y mock del repositorio
        Long idBusqueda = 1L;
        Plato plato = Plato.builder()
                .id(idBusqueda)
                .nombre("Lomo Saltado")
                .descripcion("Cortes de lomo salteado con cebolla y tomate")
                .precio(15.90)
                .disponible(true)
                .build();

        when(platoRepository.findById(idBusqueda)).thenReturn(Optional.of(plato));

        // ACT: Invocar el método a probar
        PlatoResponseDTO resultado = platoService.obtenerPorId(idBusqueda);

        // ASSERT: Verificar aserciones de valores esperados
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Lomo Saltado", resultado.getNombre());
        assertEquals("Cortes de lomo salteado con cebolla y tomate", resultado.getDescripcion());
        assertEquals(15.90, resultado.getPrecio());
        assertTrue(resultado.getDisponible());

        // VERIFY: Comprobar que se llamó a findById exactamente 1 vez
        verify(platoRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Configurar repositorio para retornar Optional vacío
        Long idInvalido = 999L;
        when(platoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            platoService.obtenerPorId(idInvalido);
        });

        assertEquals("Plato no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar invocación al repositorio
        verify(platoRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar un ID inexistente se obtiene un error HTTP 500 o NullPointerException:
         * - QA debe reportar: "Falla al buscar plato inexistente: La API responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el controlador tiene un ExceptionHandler adecuado para capturar ResourceNotFoundException.
         */
    }

    // =========================================================================
    // actualizarPlato()
    // =========================================================================

    @Test
    @DisplayName("actualizarPlato - Debe actualizar y retornar el plato modificado si el ID existe")
    void actualizarPlato_DebeRetornarPlatoResponseDTO_CuandoIdExisteYRequestEsValido() {
        // ARRANGE: Preparar datos de entrada, entidad persistida original y entidad actualizada
        Long idActualizar = 1L;

        PlatoRequestDTO request = new PlatoRequestDTO();
        request.setNombre("Lomo Saltado Premium");
        request.setDescripcion("Cortes finos de lomo salteado con cebolla, tomate y un toque de pisco");
        request.setPrecio(21.90);
        request.setDisponible(true);

        Plato platoOriginal = Plato.builder()
                .id(idActualizar)
                .nombre("Lomo Saltado")
                .descripcion("Cortes de lomo salteado con cebolla y tomate")
                .precio(15.90)
                .disponible(true)
                .build();

        Plato platoActualizado = Plato.builder()
                .id(idActualizar)
                .nombre("Lomo Saltado Premium")
                .descripcion("Cortes finos de lomo salteado con cebolla, tomate y un toque de pisco")
                .precio(21.90)
                .disponible(true)
                .build();

        when(platoRepository.findById(idActualizar)).thenReturn(Optional.of(platoOriginal));
        when(platoRepository.save(any(Plato.class))).thenReturn(platoActualizado);

        // ACT: Invocar el método del servicio a probar
        PlatoResponseDTO resultado = platoService.actualizarPlato(idActualizar, request);

        // ASSERT: Verificar aserciones de valores actualizados
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Lomo Saltado Premium", resultado.getNombre());
        assertEquals("Cortes finos de lomo salteado con cebolla, tomate y un toque de pisco", resultado.getDescripcion());
        assertEquals(21.90, resultado.getPrecio());

        // VERIFY: Comprobar llamadas al repositorio
        verify(platoRepository, times(1)).findById(idActualizar);
        verify(platoRepository, times(1)).save(any(Plato.class));
    }

    @Test
    @DisplayName("actualizarPlato - Debe lanzar ResourceNotFoundException si el ID no existe")
    void actualizarPlato_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Preparar request y repositorio retornando vacío
        Long idInvalido = 999L;
        PlatoRequestDTO request = new PlatoRequestDTO();
        request.setNombre("Plato Fantasma");
        request.setDescripcion("No existe");
        request.setPrecio(10.0);
        request.setDisponible(false);

        when(platoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            platoService.actualizarPlato(idInvalido, request);
        });

        assertEquals("Plato no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar que no se llamó a save() por no encontrar la entidad
        verify(platoRepository, times(1)).findById(idInvalido);
        verify(platoRepository, never()).save(any(Plato.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de ResourceNotFoundException se obtiene un error al persistir datos (o se guarda un nuevo registro):
         * - QA debe reportar: "Falla al actualizar plato: La actualización crea un registro nuevo en lugar de lanzar 404."
         * - Desarrollo debe revisar: Si se está validando la existencia del registro con findById antes de invocar save().
         */
    }

    // =========================================================================
    // eliminarPlato()
    // =========================================================================

    @Test
    @DisplayName("eliminarPlato - Debe eliminar el plato si el ID existe")
    void eliminarPlato_DebeEliminarPlato_CuandoIdExiste() {
        // ARRANGE: Preparar ID y entidad del plato simulado a eliminar
        Long idEliminar = 1L;
        Plato plato = Plato.builder()
                .id(idEliminar)
                .nombre("Lomo Saltado")
                .descripcion("Cortes de lomo salteado con cebolla y tomate")
                .precio(15.90)
                .disponible(true)
                .build();

        when(platoRepository.findById(idEliminar)).thenReturn(Optional.of(plato));
        doNothing().when(platoRepository).delete(plato);

        // ACT: Invocar el método del servicio a probar
        assertDoesNotThrow(() -> {
            platoService.eliminarPlato(idEliminar);
        });

        // VERIFY: Comprobar que se llamó a buscar y a eliminar exactamente 1 vez
        verify(platoRepository, times(1)).findById(idEliminar);
        verify(platoRepository, times(1)).delete(plato);
    }

    @Test
    @DisplayName("eliminarPlato - Debe lanzar ResourceNotFoundException si el ID no existe")
    void eliminarPlato_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Preparar repositorio para retornar vacío al buscar el plato
        Long idInvalido = 999L;
        when(platoRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción adecuada
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            platoService.eliminarPlato(idInvalido);
        });

        assertEquals("Plato no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar que no se llamó a delete ya que la búsqueda falló
        verify(platoRepository, times(1)).findById(idInvalido);
        verify(platoRepository, never()).delete(any(Plato.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 404 / ResourceNotFoundException se obtiene un error de base de datos o se borra un plato incorrecto:
         * - QA debe reportar: "Falla al eliminar plato: El sistema no valida la existencia antes de eliminar y causa inconsistencias."
         * - Desarrollo debe revisar: El orden de los llamados y la validación findById().orElseThrow() antes de hacer .delete().
         */
    }
}

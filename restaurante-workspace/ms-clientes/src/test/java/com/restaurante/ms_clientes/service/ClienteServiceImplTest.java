package com.restaurante.ms_clientes.service;

import com.restaurante.ms_clientes.dto.ClienteRequestDTO;
import com.restaurante.ms_clientes.dto.ClienteResponseDTO;
import com.restaurante.ms_clientes.entity.Cliente;
import com.restaurante.ms_clientes.repository.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import com.restaurante.ms_clientes.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para ClienteServiceImpl.
 *
 * Estrategia:
 * - Se utiliza MockitoExtension para pruebas unitarias puras y rápidas.
 * - Se simula la base de datos (ClienteRepository) con @Mock.
 * - Se inyectan los mocks en ClienteServiceImpl con @InjectMocks.
 * - Estructura de cada test: ARRANGE → ACT → ASSERT → VERIFY.
 */
@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    // =========================================================================
    // crearCliente()
    // =========================================================================

    @Test
    @DisplayName("crearCliente - Debe guardar y retornar el cliente mapeado a DTO")
    void crearCliente_DebeRetornarClienteResponseDTO_CuandoRequestEsValido() {
        // ARRANGE: Preparar DTO de entrada, entidad simulada y su resultado al guardar
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNombre("Carlos Gomez");
        request.setEmail("carlos.gomez@example.com");
        request.setTelefono("+56912345678");
        request.setDireccion("Av. Vitacura 1234, Santiago");

        Cliente clienteGuardado = Cliente.builder()
                .id(1L)
                .nombre("Carlos Gomez")
                .email("carlos.gomez@example.com")
                .telefono("+56912345678")
                .direccion("Av. Vitacura 1234, Santiago")
                .build();

        // Configurar Mockito para simular el comportamiento del repositorio
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // ACT: Invocar el método del servicio a probar
        ClienteResponseDTO resultado = clienteService.crearCliente(request);

        // ASSERT: Verificar que la respuesta contenga los datos mapeados correctamente
        assertNotNull(resultado, "El DTO retornado no debe ser nulo");
        assertEquals(1L, resultado.getId(), "El ID mapeado no coincide");
        assertEquals("Carlos Gomez", resultado.getNombre(), "El nombre no coincide");
        assertEquals("carlos.gomez@example.com", resultado.getEmail(), "El email no coincide");
        assertEquals("+56912345678", resultado.getTelefono(), "El teléfono no coincide");
        assertEquals("Av. Vitacura 1234, Santiago", resultado.getDireccion(), "La dirección no coincide");

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(clienteRepository, times(1)).save(any(Cliente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al crear el cliente se lanza una excepción de tipo NullPointerException:
         * - QA debe reportar: "Falla al registrar cliente: NullPointerException en el flujo de negocio."
         * - Desarrollo debe revisar: Si el builder de la entidad Cliente o el mapeador mapToDTO está accediendo a campos nulos.
         */
    }

    // =========================================================================
    // obtenerTodos()
    // =========================================================================

    @Test
    @DisplayName("obtenerTodos - Debe retornar la lista de clientes mapeada a DTO")
    void obtenerTodos_DebeRetornarListaDeClientesResponseDTO() {
        // ARRANGE: Preparar clientes de prueba en el repositorio simulado
        Cliente cliente1 = Cliente.builder()
                .id(1L)
                .nombre("Carlos Gomez")
                .email("carlos.gomez@example.com")
                .telefono("+56912345678")
                .direccion("Av. Vitacura 1234, Santiago")
                .build();

        Cliente cliente2 = Cliente.builder()
                .id(2L)
                .nombre("Maria Lopez")
                .email("maria.lopez@example.com")
                .telefono("+56987654321")
                .direccion("Calle Falsa 123, Valparaiso")
                .build();

        when(clienteRepository.findAll()).thenReturn(java.util.Arrays.asList(cliente1, cliente2));

        // ACT: Invocar el método del servicio a probar
        List<ClienteResponseDTO> resultado = clienteService.obtenerTodos();

        // ASSERT: Verificar que la lista retorne los clientes esperados mapeados
        assertNotNull(resultado, "La lista retornada no debe ser nula");
        assertEquals(2, resultado.size(), "El tamaño de la lista debe ser 2");

        ClienteResponseDTO dto1 = resultado.get(0);
        assertEquals(1L, dto1.getId());
        assertEquals("Carlos Gomez", dto1.getNombre());
        assertEquals("carlos.gomez@example.com", dto1.getEmail());

        ClienteResponseDTO dto2 = resultado.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals("Maria Lopez", dto2.getNombre());
        assertEquals("maria.lopez@example.com", dto2.getEmail());

        // VERIFY: Validar que se invocó al repositorio una única vez
        verify(clienteRepository, times(1)).findAll();

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si el servicio retorna una lista vacía habiendo datos en el repositorio:
         * - QA debe reportar: "Falla al listar clientes: La API retorna una lista vacía cuando existen clientes en base de datos."
         * - Desarrollo debe revisar: Si se está llamando correctamente al findAll() del repositorio o si hay un error en el stream/map.
         */
    }

    // =========================================================================
    // obtenerPorId()
    // =========================================================================

    @Test
    @DisplayName("obtenerPorId - Debe retornar el cliente mapeado a DTO cuando el ID existe")
    void obtenerPorId_DebeRetornarClienteResponseDTO_CuandoIdExiste() {
        // ARRANGE: Preparar entidad simulada y mock del repositorio
        Long idBusqueda = 1L;
        Cliente cliente = Cliente.builder()
                .id(idBusqueda)
                .nombre("Carlos Gomez")
                .email("carlos.gomez@example.com")
                .telefono("+56912345678")
                .direccion("Av. Vitacura 1234, Santiago")
                .build();

        when(clienteRepository.findById(idBusqueda)).thenReturn(Optional.of(cliente));

        // ACT: Invocar el método a probar
        ClienteResponseDTO resultado = clienteService.obtenerPorId(idBusqueda);

        // ASSERT: Verificar aserciones de valores esperados
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Carlos Gomez", resultado.getNombre());
        assertEquals("carlos.gomez@example.com", resultado.getEmail());
        assertEquals("+56912345678", resultado.getTelefono());
        assertEquals("Av. Vitacura 1234, Santiago", resultado.getDireccion());

        // VERIFY: Comprobar que se llamó a findById exactamente 1 vez
        verify(clienteRepository, times(1)).findById(idBusqueda);
    }

    @Test
    @DisplayName("obtenerPorId - Debe lanzar ResourceNotFoundException cuando el ID no existe")
    void obtenerPorId_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Configurar repositorio para retornar Optional vacío
        Long idInvalido = 999L;
        when(clienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.obtenerPorId(idInvalido);
        });

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar invocación al repositorio
        verify(clienteRepository, times(1)).findById(idInvalido);

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si al buscar un ID inexistente se obtiene un error HTTP 500 o NullPointerException:
         * - QA debe reportar: "Falla al buscar cliente inexistente: La API responde con 500 en lugar de 404 Not Found."
         * - Desarrollo debe revisar: Si el controlador tiene un ExceptionHandler adecuado para capturar ResourceNotFoundException.
         */
    }

    // =========================================================================
    // actualizarCliente()
    // =========================================================================

    @Test
    @DisplayName("actualizarCliente - Debe actualizar y retornar el cliente modificado si el ID existe")
    void actualizarCliente_DebeRetornarClienteResponseDTO_CuandoIdExisteYRequestEsValido() {
        // ARRANGE: Preparar datos de entrada, entidad persistida original y entidad actualizada
        Long idActualizar = 1L;

        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNombre("Carlos Gomez Modificado");
        request.setEmail("carlos.gomez.new@example.com");
        request.setTelefono("+56999999999");
        request.setDireccion("Calle Nueva 5678, Providencia");

        Cliente clienteOriginal = Cliente.builder()
                .id(idActualizar)
                .nombre("Carlos Gomez")
                .email("carlos.gomez@example.com")
                .telefono("+56912345678")
                .direccion("Av. Vitacura 1234, Santiago")
                .build();

        Cliente clienteActualizado = Cliente.builder()
                .id(idActualizar)
                .nombre("Carlos Gomez Modificado")
                .email("carlos.gomez.new@example.com")
                .telefono("+56999999999")
                .direccion("Calle Nueva 5678, Providencia")
                .build();

        when(clienteRepository.findById(idActualizar)).thenReturn(Optional.of(clienteOriginal));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActualizado);

        // ACT: Invocar el método del servicio a probar
        ClienteResponseDTO resultado = clienteService.actualizarCliente(idActualizar, request);

        // ASSERT: Verificar aserciones de valores actualizados
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Carlos Gomez Modificado", resultado.getNombre());
        assertEquals("carlos.gomez.new@example.com", resultado.getEmail());
        assertEquals("+56999999999", resultado.getTelefono());
        assertEquals("Calle Nueva 5678, Providencia", resultado.getDireccion());

        // VERIFY: Comprobar llamadas al repositorio
        verify(clienteRepository, times(1)).findById(idActualizar);
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("actualizarCliente - Debe lanzar ResourceNotFoundException si el ID no existe")
    void actualizarCliente_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Preparar request y repositorio retornando vacío
        Long idInvalido = 999L;
        ClienteRequestDTO request = new ClienteRequestDTO();
        request.setNombre("Cliente Fantasma");
        request.setEmail("fantasma@example.com");
        request.setTelefono("+56900000000");
        request.setDireccion("No existe");

        when(clienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción correcta
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.actualizarCliente(idInvalido, request);
        });

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar que no se llamó a save() por no encontrar la entidad
        verify(clienteRepository, times(1)).findById(idInvalido);
        verify(clienteRepository, never()).save(any(Cliente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de ResourceNotFoundException se obtiene un error al persistir datos (o se guarda un nuevo registro):
         * - QA debe reportar: "Falla al actualizar cliente: La actualización crea un registro nuevo en lugar de lanzar 404."
         * - Desarrollo debe revisar: Si se está validando la existencia del registro con findById antes de invocar save().
         */
    }

    // =========================================================================
    // eliminarCliente()
    // =========================================================================

    @Test
    @DisplayName("eliminarCliente - Debe eliminar el cliente si el ID existe")
    void eliminarCliente_DebeEliminarCliente_CuandoIdExiste() {
        // ARRANGE: Preparar ID y entidad del cliente simulado a eliminar
        Long idEliminar = 1L;
        Cliente cliente = Cliente.builder()
                .id(idEliminar)
                .nombre("Carlos Gomez")
                .email("carlos.gomez@example.com")
                .telefono("+56912345678")
                .direccion("Av. Vitacura 1234, Santiago")
                .build();

        when(clienteRepository.findById(idEliminar)).thenReturn(Optional.of(cliente));
        doNothing().when(clienteRepository).delete(cliente);

        // ACT: Invocar el método del servicio a probar
        assertDoesNotThrow(() -> {
            clienteService.eliminarCliente(idEliminar);
        });

        // VERIFY: Comprobar que se llamó a buscar y a eliminar exactamente 1 vez
        verify(clienteRepository, times(1)).findById(idEliminar);
        verify(clienteRepository, times(1)).delete(cliente);
    }

    @Test
    @DisplayName("eliminarCliente - Debe lanzar ResourceNotFoundException si el ID no existe")
    void eliminarCliente_DebeLanzarResourceNotFoundException_CuandoIdNoExiste() {
        // ARRANGE: Preparar repositorio para retornar vacío al buscar el cliente
        Long idInvalido = 999L;
        when(clienteRepository.findById(idInvalido)).thenReturn(Optional.empty());

        // ACT & ASSERT: Verificar que se lanza la excepción adecuada
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            clienteService.eliminarCliente(idInvalido);
        });

        assertEquals("Cliente no encontrado con ID: 999", exception.getMessage());

        // VERIFY: Comprobar que no se llamó a delete ya que la búsqueda falló
        verify(clienteRepository, times(1)).findById(idInvalido);
        verify(clienteRepository, never()).delete(any(Cliente.class));

        /*
         * CASO HIPOTÉTICO DE FALLA PARA QA:
         * Si en lugar de HTTP 404 / ResourceNotFoundException se obtiene un error de base de datos o se borra un cliente incorrecto:
         * - QA debe reportar: "Falla al eliminar cliente: El sistema no valida la existencia antes de eliminar y causa inconsistencias."
         * - Desarrollo debe revisar: El orden de los llamados y la validación findById().orElseThrow() antes de hacer .delete().
         */
    }
}

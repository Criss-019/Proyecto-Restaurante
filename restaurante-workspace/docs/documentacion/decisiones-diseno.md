# 🧠 Decisiones de Diseño y Arquitectura

> Documento que explica el **"por qué"** detrás de las elecciones tecnológicas y de diseño del sistema.  
> Útil para nuevos integrantes del equipo, revisiones de código y defensa del proyecto.

---

## 1. Arquitectura de Microservicios

### Decisión
Implementar el sistema como un conjunto de **microservicios independientes** en lugar de una aplicación monolítica.

### Justificación

| Criterio | Monolito | Microservicios (elegido) |
|----------|----------|--------------------------|
| Escalabilidad | Todo o nada | Escalar solo lo necesario (ej: ms-pedidos) |
| Despliegue | Toda la app o nada | Desplegar servicios individualmente |
| Tolerancia a fallos | Un fallo derrumba todo | Un servicio caído no afecta los demás |
| Tecnología | Una sola tecnología | Cada servicio puede usar tech diferente |
| Complejidad | Baja (inicio) | Alta (pero manejable con Spring Cloud) |

> **Conclusión:** Para un sistema de restaurante con múltiples dominios operativos distintos (catálogo, pagos, despacho, notificaciones), la arquitectura de microservicios permite que cada área crezca y falle de forma independiente.

---

## 2. Spring Boot + Spring Cloud

### Decisión
Usar **Spring Boot 3.x** como framework base y **Spring Cloud** para las capacidades de microservicios.

### Justificación

- **Spring Boot:** El estándar de facto para microservicios en Java. Configuración mínima, servidor embebido Tomcat incluido, y ecosistema maduro con Spring Data JPA, Spring Web, Validation, etc.

- **Spring Cloud Netflix Eureka:** Para el descubrimiento de servicios. En lugar de hardcodear URLs (`http://localhost:8083`), los servicios se registran con su nombre (`ms-clientes`) y Spring Cloud los resuelve dinámicamente. Esto permite escalar servicios sin cambiar código.

- **Spring Cloud Gateway:** API Gateway reactivo que centraliza todas las peticiones externas. Permite agregar filtros de autenticación, rate limiting y CORS en un solo punto.

- **Spring Cloud OpenFeign:** Permite hacer llamadas HTTP entre microservicios de forma declarativa (interfaces Java con anotaciones), en lugar de usar `RestTemplate` o `WebClient` manualmente. El resultado es código más limpio y testeble.

```java
// Sin Feign: código verboso y frágil
RestTemplate rest = new RestTemplate();
ClienteDTO cliente = rest.getForObject("http://ms-clientes/api/clientes/" + id, ClienteDTO.class);

// Con Feign: declarativo y limpio ✅
@FeignClient(name = "ms-clientes")
public interface ClienteClient {
    @GetMapping("/api/clientes/{id}")
    ClienteDTO obtenerPorId(@PathVariable Long id);
}
```

---

## 3. Patrón Database per Service

### Decisión
Cada microservicio tiene su **propia base de datos independiente** (10 bases de datos MySQL separadas), sin compartir tablas ni esquemas entre servicios.

### Justificación

| Problema | Bases de datos compartidas | Database per Service (elegido) |
|----------|---------------------------|-------------------------------|
| Acoplamiento | Altísimo — cambiar una tabla rompe otros servicios | Bajo — cada servicio es dueño absoluto de sus datos |
| Escalabilidad | Cuellos de botella en la DB compartida | Cada DB escala con su servicio |
| Autonomía | Equipos se bloquean mutuamente | Cada equipo puede cambiar su esquema |
| Transacciones distribuidas | Simples (ACID locales) | Requieren sagas o eventos (Compensación) |

> **Compensación:** Las relaciones entre entidades de distintos servicios son **lógicas**, no físicas. Por ejemplo, `pedidos_db.cliente_id` no tiene una FK física hacia `clientes_db.clientes.id`. La integridad se garantiza a nivel de aplicación mediante llamadas Feign antes de persistir.

---

## 4. Comunicación Sincrónica con OpenFeign

### Decisión
Usar **REST sincrónico vía OpenFeign** para la comunicación inter-servicios, en lugar de mensajería asíncrona (RabbitMQ / Kafka).

### Justificación

- El flujo de negocio es **secuencial y dependiente**: no se puede crear un pedido sin verificar que el cliente existe. Esta dependencia hace que la sincronía sea la opción natural.
- Para el alcance de este proyecto (académico/demostración), la sincronía es más simple de implementar, depurar y entender.
- Los puntos de comunicación asíncrona (notificaciones, facturación) están protegidos con `try-catch` para que el fallo sea silencioso y no bloquee el flujo principal.

> **Evolución futura:** Si el sistema creciera a producción real, los eventos asíncronos como "PedidoCreado" o "PagoAprobado" serían publicados en **RabbitMQ** o **Apache Kafka**, y los servicios consumidores (notificaciones, facturación) reaccionarían de forma desacoplada.

---

## 5. Patrón DTO (Data Transfer Object)

### Decisión
Usar **DTOs separados** para request y response en todos los microservicios, en lugar de exponer directamente las entidades JPA.

### Justificación

```
Request DTO  →  Servicio  →  Entidad (guardada en BD)
Entidad      →  Servicio  →  Response DTO (retornado al cliente)
```

- **Seguridad:** Evita que campos internos como `id`, `fechaCreacion` o `estado` sean enviados o sobreescritos por el cliente.
- **Evolución:** El schema de BD puede cambiar sin romper el contrato de la API.
- **Validación:** Las anotaciones `@NotNull`, `@NotBlank`, `@Min` se aplican en el DTO, no en la entidad.

---

## 6. MySQL como Motor de Base de Datos

### Decisión
Usar **MySQL 8.x** (vía XAMPP) como motor de bases de datos relacional.

### Justificación

- **Compatibilidad:** Soporte nativo y maduro con Spring Data JPA / Hibernate.
- **Facilidad de setup local:** XAMPP ofrece MySQL + phpMyAdmin con un click, reduciendo la barrera de entrada para el equipo.
- **Ampliamente conocido:** El equipo tiene experiencia previa con MySQL.

> **Alternativa considerada:** PostgreSQL ofrece características avanzadas (JSONB, full-text search, mejor manejo de concurrencia), pero MySQL fue suficiente para los requerimientos actuales y simplificó el setup local.

---

## 7. Eureka como Servidor de Descubrimiento

### Decisión
Usar **Netflix Eureka** para el registro y descubrimiento dinámico de servicios.

### Justificación

- Los microservicios no necesitan conocer la IP o puerto exacto de otros servicios.
- Permite que múltiples instancias de un mismo servicio se registren (preparación para escalabilidad horizontal).
- El API Gateway usa Eureka automáticamente para resolver las rutas hacia los microservicios.
- Alternativas como **Consul** o **Zookeeper** son más robustas en producción, pero Eureka es el más simple de configurar con Spring Cloud.

---

## 8. Manejo de IVA en ms-facturacion

### Decisión
Calcular automáticamente el IVA (19%) en el servicio de facturación, con la tasa definida como constante de negocio.

### Justificación

```java
private static final double PORCENTAJE_IMPUESTO = 0.19; // IVA Chile

double impuestos = subtotal * PORCENTAJE_IMPUESTO;
double total     = subtotal + impuestos;
```

- El IVA es responsabilidad exclusiva del dominio de facturación — ms-pagos no calcula impuestos, solo registra el monto pagado.
- La tasa está centralizada como constante, facilitando un futuro cambio regulatorio.
- El `numeroFactura` se genera con `UUID` truncado (`FAC-XXXXXXXX`) simulando un folio fiscal real.

---

## 9. Tolerancia a Fallos con try-catch en Feign

### Decisión
Envolver todas las llamadas Feign en bloques `try-catch` con estrategias de fallback diferenciadas.

### Justificación

```java
// Crítico: si ms-clientes falla, el pedido NO se crea
try {
    cliente = clienteClient.obtenerPorId(id);
} catch (FeignException.NotFound e) {
    throw new ResourceNotFoundException("El cliente no existe"); // Propaga el error
}

// No crítico: si ms-notificaciones falla, el pedido SÍ se crea
try {
    notificacionClient.enviarNotificacion(notificacion);
} catch (FeignException e) {
    log.error("Falló la notificación: {}", e.getMessage()); // Solo loguea
}
```

> **Principio:** La tolerancia a fallos debe ser **proporcional a la criticidad del flujo**. Una notificación no debe bloquear la creación de un pedido, pero la verificación del cliente sí.

> **Evolución futura:** Implementar **Resilience4j** con circuit breakers para evitar cascadas de fallos y definir políticas de reintento automático.

---

## 10. Lombok para Reducir Código Boilerplate

### Decisión
Usar **Lombok** en todas las entidades y DTOs.

### Justificación

```java
// Sin Lombok: 60+ líneas de getters, setters, constructores
public class Pedido {
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    // ... etc
}

// Con Lombok: conciso y legible ✅
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Pedido {
    private Long id;
    // ... solo los campos
}
```

- Reduce drásticamente el ruido en el código.
- Los IDEs modernos (IntelliJ, Eclipse) tienen soporte nativo de Lombok.
- El `@Builder` pattern facilita la construcción de objetos en los servicios de forma fluida.

---

## Resumen de Decisiones

| Decisión | Tecnología elegida | Alternativas descartadas |
|----------|-------------------|--------------------------|
| Framework | Spring Boot 3.x | Quarkus, Micronaut |
| Descubrimiento | Eureka | Consul, Zookeeper |
| Gateway | Spring Cloud Gateway | Nginx, Kong |
| Feign | OpenFeign | RestTemplate, WebClient |
| Base de datos | MySQL 8 (por servicio) | PostgreSQL, H2, MongoDB |
| ORM | Hibernate/JPA | MyBatis, JDBC puro |
| Mensajería | — (sincrónico) | RabbitMQ, Kafka (futuro) |
| Boilerplate | Lombok | Records Java 16+ |
| Resiliencia | try-catch básico | Resilience4j (futuro) |

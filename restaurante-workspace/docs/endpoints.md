# 📡 Contrato de API — Sistema de Gestión de Restaurante

> **Arquitectura:** Microservicios con Spring Boot + Spring Cloud  
> **Punto de entrada único (API Gateway):** `http://localhost:8080`  
> **Descubrimiento de servicios (Eureka):** `http://localhost:8761`  
> **Formato de datos:** `application/json` en todos los endpoints  
> **Fecha de última actualización:** 2026-06-30

---

## 🗺️ Mapa rápido de rutas base

| Microservicio      | Puerto Interno | Ruta base (vía Gateway)          |
|--------------------|:--------------:|----------------------------------|
| ms-catalogo        | `8081`         | `/api/catalogo/platos`           |
| ms-stock           | `8082`         | `/api/stock/ingredientes`        |
| ms-clientes        | `8083`         | `/api/clientes`                  |
| ms-reservas        | `8084`         | `/api/reservas`                  |
| ms-cocina          | `8085`         | `/api/cocina/comandas`           |
| ms-pedidos         | `8086`         | `/api/pedidos`                   |
| ms-pagos           | `8087`         | `/api/pagos`                     |
| ms-despacho        | `8088`         | `/api/despachos`                 |
| ms-facturacion     | `8089`         | `/api/facturacion`               |
| ms-notificaciones  | `8090`         | `/api/notificaciones`            |

---

## 1. 🍽️ ms-catalogo — Menú del Restaurante

> Administra el catálogo de platos y bebidas disponibles para ordenar.

**URL Base:** `http://localhost:8080/api/catalogo/platos`

---

### `POST /api/catalogo/platos`
Registra un nuevo plato o bebida en el menú.

**Request Body:**
```json
{
  "nombre":      "Lomo Saltado",
  "descripcion": "Salteado de lomo, tomate, cebolla y papas fritas",
  "precio":      8500.00,
  "disponible":  true
}
```

**Response Body (`201 Created`):**
```json
{
  "id":          1,
  "nombre":      "Lomo Saltado",
  "descripcion": "Salteado de lomo, tomate, cebolla y papas fritas",
  "precio":      8500.00,
  "disponible":  true
}
```

| Código | Descripción                                |
|--------|--------------------------------------------|
| `201`  | Plato creado exitosamente                  |
| `400`  | Campos requeridos faltantes o inválidos    |
| `409`  | Ya existe un plato con ese nombre          |

---

### `GET /api/catalogo/platos`
Retorna todos los platos del menú.

**Response Body (`200 OK`):**
```json
[
  { "id": 1, "nombre": "Lomo Saltado",     "precio": 8500.00, "disponible": true },
  { "id": 2, "nombre": "Pastel de Choclo", "precio": 7200.00, "disponible": true }
]
```

---

### `GET /api/catalogo/platos/{id}`
Retorna un plato específico por su ID.

**Path Variable:** `id` — ID numérico del plato

**Response Body (`200 OK`):**
```json
{ "id": 1, "nombre": "Lomo Saltado", "descripcion": "...", "precio": 8500.00, "disponible": true }
```

| Código | Descripción              |
|--------|--------------------------|
| `200`  | Plato encontrado         |
| `404`  | Plato no encontrado      |

---

### `PUT /api/catalogo/platos/{id}`
Actualiza un plato existente (nombre, precio, disponibilidad).

**Request Body:** igual al de `POST`  
**Response Body (`200 OK`):** igual al de `POST`

| Código | Descripción              |
|--------|--------------------------|
| `200`  | Plato actualizado        |
| `400`  | Datos inválidos          |
| `404`  | Plato no encontrado      |

---

### `DELETE /api/catalogo/platos/{id}`
Elimina un plato del catálogo.

**Response:** `204 No Content`

---

## 2. 📦 ms-stock — Inventario de Bodega

> Controla el stock de ingredientes y materias primas disponibles.

**URL Base:** `http://localhost:8080/api/stock/ingredientes`

---

### `POST /api/stock/ingredientes`
Registra un nuevo ingrediente en el inventario.

**Request Body:**
```json
{
  "nombre":          "Lomo de Vacuno",
  "cantidadActual":  5000.0,
  "cantidadMinima":  1000.0,
  "unidadMedida":    "Gramos"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":              1,
  "nombre":          "Lomo de Vacuno",
  "cantidadActual":  5000.0,
  "cantidadMinima":  1000.0,
  "unidadMedida":    "Gramos"
}
```

| Código | Descripción                             |
|--------|-----------------------------------------|
| `201`  | Ingrediente registrado                  |
| `400`  | Datos inválidos                         |
| `409`  | Ya existe un ingrediente con ese nombre |

---

### `GET /api/stock/ingredientes`
Lista todos los ingredientes del inventario.

---

### `GET /api/stock/ingredientes/{id}`
Obtiene un ingrediente por ID.

| Código | Descripción                   |
|--------|-------------------------------|
| `200`  | Ingrediente encontrado        |
| `404`  | Ingrediente no encontrado     |

---

### `PUT /api/stock/ingredientes/{id}`
Actualiza cantidad o unidad de un ingrediente.

**Request Body:** igual al de `POST`

---

### `DELETE /api/stock/ingredientes/{id}`
Elimina un ingrediente del inventario.

**Response:** `204 No Content`

---

## 3. 👤 ms-clientes — Gestión de Clientes

> Administra los perfiles de clientes. Es consultado por ms-pedidos al crear una orden.

**URL Base:** `http://localhost:8080/api/clientes`

---

### `POST /api/clientes`
Registra un nuevo cliente en el sistema.

**Request Body:**
```json
{
  "nombre":    "Cristián Carvajal",
  "email":     "cristian.carvajal@email.cl",
  "telefono":  "+56912345678",
  "direccion": "Av. Providencia 1234, Santiago"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":        1,
  "nombre":    "Cristián Carvajal",
  "email":     "cristian.carvajal@email.cl",
  "telefono":  "+56912345678",
  "direccion": "Av. Providencia 1234, Santiago"
}
```

| Código | Descripción                        |
|--------|------------------------------------|
| `201`  | Cliente creado                     |
| `400`  | Campos requeridos faltantes        |
| `409`  | Ya existe un cliente con ese email |

---

### `GET /api/clientes`
Lista todos los clientes registrados.

---

### `GET /api/clientes/{id}`
Obtiene un cliente por ID. ⚠️ **Este endpoint es invocado internamente por ms-pedidos vía Feign.**

| Código | Descripción            |
|--------|------------------------|
| `200`  | Cliente encontrado     |
| `404`  | Cliente no encontrado  |

---

### `PUT /api/clientes/{id}`
Actualiza datos del cliente.

---

### `DELETE /api/clientes/{id}`
Elimina un cliente.

**Response:** `204 No Content`

---

## 4. 📅 ms-reservas — Gestión de Reservas

> Agenda mesas y horarios. Independiente del ciclo de pedidos.

**URL Base:** `http://localhost:8080/api/reservas`

---

### `POST /api/reservas`
Crea una nueva reserva de mesa.

**Request Body:**
```json
{
  "clienteId":        1,
  "fechaHora":        "2026-07-01T13:00:00",
  "cantidadPersonas": 2,
  "estado":           "PENDIENTE",
  "observaciones":    "Mesa con vista al jardín"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":               1,
  "clienteId":        1,
  "fechaHora":        "2026-07-01T13:00:00",
  "cantidadPersonas": 2,
  "estado":           "PENDIENTE",
  "observaciones":    "Mesa con vista al jardín"
}
```

| Código | Descripción                  |
|--------|------------------------------|
| `201`  | Reserva creada               |
| `400`  | Datos inválidos              |

---

### `GET /api/reservas`
Lista todas las reservas.

---

### `GET /api/reservas/{id}`
Obtiene una reserva por ID.

| Código | Descripción              |
|--------|--------------------------|
| `200`  | Reserva encontrada       |
| `404`  | Reserva no encontrada    |

---

### `PUT /api/reservas/{id}`
Actualiza datos de una reserva.

---

### `PATCH /api/reservas/{id}/estado`
Cambia el estado de una reserva.

**Query Param:** `?estado=CONFIRMADA`  
**Estados válidos:** `PENDIENTE`, `CONFIRMADA`, `CANCELADA`

**Response Body (`200 OK`):** Reserva actualizada.

---

### `DELETE /api/reservas/{id}`
Elimina una reserva.

**Response:** `204 No Content`

---

## 5. 👨‍🍳 ms-cocina — Comandas de Cocina

> Gestiona las órdenes de preparación para los chefs.

**URL Base:** `http://localhost:8080/api/cocina/comandas`

---

### `POST /api/cocina/comandas`
Crea una comanda para que cocina prepare un plato.

**Request Body:**
```json
{
  "pedidoId": 1,
  "platoId":  1,
  "cantidad": 2,
  "estado":   "PENDIENTE",
  "notas":    "Sin cebolla, término medio"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":       1,
  "pedidoId": 1,
  "platoId":  1,
  "cantidad": 2,
  "estado":   "PENDIENTE",
  "notas":    "Sin cebolla, término medio"
}
```

| Código | Descripción              |
|--------|--------------------------|
| `201`  | Comanda registrada       |
| `400`  | Datos inválidos          |

---

### `GET /api/cocina/comandas`
Lista todas las comandas activas.

---

### `GET /api/cocina/comandas/{id}`
Obtiene una comanda por ID.

---

### `GET /api/cocina/comandas/pedido/{pedidoId}`
Lista todas las comandas asociadas a un pedido específico.

**Response Body (`200 OK`):** Array de comandas.

| Código | Descripción                                     |
|--------|-------------------------------------------------|
| `200`  | Lista de comandas (puede ser vacía `[]`)        |

---

### `PUT /api/cocina/comandas/{id}`
Actualiza una comanda completa.

---

### `PATCH /api/cocina/comandas/{id}/estado`
Cambia el estado de una comanda en tiempo real.

**Query Param:** `?estado=EN_PREPARACION`  
**Estados válidos:** `PENDIENTE`, `EN_PREPARACION`, `LISTO`

---

### `DELETE /api/cocina/comandas/{id}`
Elimina una comanda.

**Response:** `204 No Content`

---

## 6. 🧾 ms-pedidos — Orquestador de Pedidos ⭐

> Corazón del sistema. Orquesta el ciclo de vida completo de una orden.  
> Al crear un pedido: **verifica cliente** (Feign → ms-clientes) y **envía notificación** (Feign → ms-notificaciones).

**URL Base:** `http://localhost:8080/api/pedidos`

---

### `POST /api/pedidos`
Crea un nuevo pedido. Valida existencia del cliente y dispara notificación automática.

**Request Body:**
```json
{
  "clienteId":   1,
  "tipoEntrega": "DELIVERY"
}
```
> **Nota:** `estado` y `total` son asignados automáticamente por el sistema (`CREADO` y `0.0`).

**Response Body (`201 Created`):**
```json
{
  "id":           1,
  "clienteId":    1,
  "fechaPedido":  "2026-06-29T13:15:00",
  "estado":       "CREADO",
  "tipoEntrega":  "DELIVERY",
  "total":        0.0
}
```

| Código | Descripción                                               |
|--------|-----------------------------------------------------------|
| `201`  | Pedido creado, notificación enviada al cliente            |
| `400`  | Datos inválidos                                           |
| `404`  | El cliente con ese ID no existe en ms-clientes            |
| `500`  | Error interno al verificar cliente o enviar notificación  |

---

### `GET /api/pedidos`
Lista todos los pedidos del sistema.

---

### `GET /api/pedidos/{id}`
Obtiene un pedido específico por ID.

| Código | Descripción            |
|--------|------------------------|
| `200`  | Pedido encontrado      |
| `404`  | Pedido no encontrado   |

---

### `GET /api/pedidos/cliente/{clienteId}`
Historial de pedidos de un cliente específico.

**Response Body (`200 OK`):** Array de pedidos del cliente.

---

### `PUT /api/pedidos/{id}`
Actualiza los datos editables de un pedido (clienteId, tipoEntrega).

---

### `PUT /api/pedidos/{id}/estado`
⚠️ **Endpoint interno crítico.** Cambia el estado del pedido.  
Invocado por ms-pagos (→ `PAGADO`) y ms-despacho (→ `ENTREGADO`) vía OpenFeign.

**Query Param:** `?estado=PAGADO`  
**Estados válidos:** `CREADO`, `PAGADO`, `ENTREGADO`, `CANCELADO`

**Response Body (`200 OK`):** Pedido con estado actualizado.

---

### `DELETE /api/pedidos/{id}`
Elimina un pedido.

**Response:** `204 No Content`

---

## 7. 💳 ms-pagos — Registro de Pagos

> Procesa transacciones financieras.  
> Al registrar un pago: llama (Feign) a **ms-pedidos** (→ `PAGADO`) y a **ms-facturacion** (emite factura).

**URL Base:** `http://localhost:8080/api/pagos`

---

### `POST /api/pagos`
Registra un pago. Dispara automáticamente la actualización del pedido y la emisión de factura.

**Request Body:**
```json
{
  "pedidoId":   1,
  "monto":      10000.00,
  "metodoPago": "TARJETA_CREDITO"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":          1,
  "pedidoId":    1,
  "monto":       10000.00,
  "metodoPago":  "TARJETA_CREDITO",
  "fechaPago":   "2026-06-29T13:20:00",
  "estado":      "APROBADO"
}
```

| Código | Descripción                                                   |
|--------|---------------------------------------------------------------|
| `201`  | Pago registrado, pedido actualizado, factura emitida          |
| `400`  | Datos inválidos o monto negativo                              |

> **Métodos de pago válidos:** `EFECTIVO`, `TARJETA_CREDITO`, `DEBITO`, `TRANSFERENCIA`

---

### `GET /api/pagos`
Lista todos los pagos registrados.

---

### `GET /api/pagos/{id}`
Obtiene un pago por ID.

---

### `GET /api/pagos/pedido/{pedidoId}`
Lista todos los pagos asociados a un pedido específico.

---

### `PUT /api/pagos/{id}`
Actualiza datos de un pago (no modifica `fechaPago` por integridad contable).

---

### `PATCH /api/pagos/{id}/estado`
Cambia el estado de un pago (ej. para emitir un reembolso).

**Query Param:** `?estado=REEMBOLSADO`  
**Estados válidos:** `APROBADO`, `RECHAZADO`, `REEMBOLSADO`

---

### `DELETE /api/pagos/{id}`
Elimina un registro de pago.

**Response:** `204 No Content`

---

## 8. 🚗 ms-despacho — Logística de Entrega

> Administra el despacho a domicilio.  
> Al marcar `ENTREGADO`: llama (Feign) a **ms-pedidos** para cerrar la orden.

**URL Base:** `http://localhost:8080/api/despachos`

---

### `POST /api/despachos`
Programa un despacho para un pedido. Estado inicial: `EN_PREPARACION`.

**Request Body:**
```json
{
  "pedidoId":              1,
  "repartidorAsignado":    "Carlos Repartidor",
  "direccionEntrega":      "Av. Providencia 1234, Santiago",
  "estado":                "EN_PREPARACION",
  "fechaEntregaEstimada":  "2026-06-29T13:50:00"
}
```

**Response Body (`201 Created`):**
```json
{
  "id":                    1,
  "pedidoId":              1,
  "repartidorAsignado":    "Carlos Repartidor",
  "direccionEntrega":      "Av. Providencia 1234, Santiago",
  "estado":                "EN_PREPARACION",
  "fechaSalida":           null,
  "fechaEntregaEstimada":  "2026-06-29T13:50:00",
  "fechaEntrega":          null
}
```

| Código | Descripción                  |
|--------|------------------------------|
| `201`  | Despacho programado          |
| `400`  | Datos inválidos              |

---

### `GET /api/despachos`
Lista todos los despachos.

---

### `GET /api/despachos/{id}`
Obtiene un despacho por ID.

---

### `GET /api/despachos/pedido/{pedidoId}`
Obtiene el despacho asociado a un pedido específico.

| Código | Descripción                              |
|--------|------------------------------------------|
| `200`  | Despacho encontrado                      |
| `404`  | No hay despacho registrado para ese pedido |

---

### `PUT /api/despachos/{id}`
⚠️ **Endpoint crítico.** Actualiza el despacho.  
Si `estado` cambia a `ENTREGADO`: registra `fechaEntrega` y notifica a ms-pedidos vía Feign.

**Request Body:** igual al de `POST`  
**Flujo de estados:** `EN_PREPARACION` → `EN_RUTA` → `ENTREGADO` | `RECHAZADO`

---

### `PATCH /api/despachos/{id}/estado`
Cambia el estado logístico del despacho.  
Al pasar a `EN_RUTA`, registra automáticamente `fechaSalida`.

**Query Param:** `?estado=EN_RUTA`

---

### `DELETE /api/despachos/{id}`
Elimina un despacho.

**Response:** `204 No Content`

---

## 9. 🧾 ms-facturacion — Documentos Fiscales

> Emite boletas y facturas con IVA calculado automáticamente (19%).  
> Es invocado automáticamente por **ms-pagos** al registrar un pago exitoso.

**URL Base:** `http://localhost:8080/api/facturacion`

---

### `POST /api/facturacion`
Emite una factura. Calcula IVA = `subtotal * 0.19` y genera folio único `FAC-XXXXXXXX`.

**Request Body:**
```json
{
  "pedidoId": 1,
  "subtotal": 10000.00
}
```

**Response Body (`201 Created`):**
```json
{
  "id":            1,
  "pedidoId":      1,
  "numeroFactura": "FAC-A1B2C3D4",
  "subtotal":      10000.00,
  "impuestos":     1900.00,
  "total":         11900.00,
  "fechaEmision":  "2026-06-29T13:21:00",
  "estadoFiscal":  "EMITIDA",
  "urlPdf":        "https://restaurante.com/facturas/FAC-A1B2C3D4.pdf"
}
```

| Código | Descripción                   |
|--------|-------------------------------|
| `201`  | Factura emitida con IVA       |
| `400`  | Datos inválidos               |

---

### `GET /api/facturacion`
Lista todas las facturas emitidas.

---

### `GET /api/facturacion/{id}`
Obtiene una factura por ID.

---

### `GET /api/facturacion/pedido/{pedidoId}`
Obtiene la factura asociada a un pedido específico.

| Código | Descripción                                  |
|--------|----------------------------------------------|
| `200`  | Factura encontrada                           |
| `404`  | No existe factura para ese pedido            |

---

### `PUT /api/facturacion/{id}`
Actualiza el subtotal de una factura (recalcula IVA automáticamente).

---

### `PATCH /api/facturacion/{id}/estado`
Cambia el estado fiscal de una factura.

**Query Param:** `?estado=ANULADA`  
**Estados válidos:** `EMITIDA`, `ANULADA`, `RECHAZADA_POR_ENTE`

---

### `DELETE /api/facturacion/{id}`
Elimina el registro de una factura.

**Response:** `204 No Content`

---

## 10. 🔔 ms-notificaciones — Centro de Alertas

> Gestiona y almacena el historial de notificaciones enviadas a clientes.  
> Es invocado por **ms-pedidos** al crear una nueva orden.

**URL Base:** `http://localhost:8080/api/notificaciones`

---

### `POST /api/notificaciones`
Registra y envía una notificación al cliente.

**Request Body:**
```json
{
  "clienteId":   1,
  "destinatario": "cristian.carvajal@email.cl",
  "tipo":         "EMAIL",
  "asunto":       "¡Pedido Recibido!",
  "mensaje":      "Hola Cristián, hemos recibido tu pedido #1. Lo estamos preparando."
}
```

**Response Body (`201 Created`):**
```json
{
  "id":             1,
  "clienteId":      1,
  "destinatario":   "cristian.carvajal@email.cl",
  "tipo":           "EMAIL",
  "asunto":         "¡Pedido Recibido!",
  "mensaje":        "Hola Cristián, hemos recibido tu pedido #1. Lo estamos preparando.",
  "estado":         "ENVIADO",
  "fechaCreacion":  "2026-06-29T13:15:30",
  "fechaEnvio":     "2026-06-29T13:15:32"
}
```

| Código | Descripción                  |
|--------|------------------------------|
| `201`  | Notificación enviada         |
| `400`  | Datos inválidos              |

> **Tipos de canal válidos:** `EMAIL`, `SMS`, `PUSH`

---

### `GET /api/notificaciones`
Lista todas las notificaciones del historial.

---

### `GET /api/notificaciones/{id}`
Obtiene una notificación por ID.

---

### `GET /api/notificaciones/cliente/{clienteId}`
Lista todas las notificaciones enviadas a un cliente específico.

---

### `PATCH /api/notificaciones/{id}/estado`
Actualiza el estado de una notificación.

**Query Param:** `?estado=FALLIDO`  
**Estados válidos:** `PENDIENTE`, `ENVIADO`, `FALLIDO`

---

### `DELETE /api/notificaciones/{id}`
Elimina el registro de una notificación.

**Response:** `204 No Content`

---

## 🔗 Resumen de Comunicaciones Inter-Servicios (OpenFeign)

| Llamador         | Destino              | Cuándo se invoca                                  | Acción                                  |
|------------------|----------------------|---------------------------------------------------|-----------------------------------------|
| **ms-pedidos**   | ms-clientes          | Al crear un pedido                                | Verifica que el cliente exista          |
| **ms-pedidos**   | ms-notificaciones    | Al crear un pedido (exitoso)                      | Envía email de confirmación al cliente  |
| **ms-pagos**     | ms-pedidos           | Al registrar un pago exitoso                      | Actualiza estado del pedido → `PAGADO`  |
| **ms-pagos**     | ms-facturacion       | Al registrar un pago exitoso                      | Emite factura con IVA automático        |
| **ms-despacho**  | ms-pedidos           | Al actualizar despacho con estado `ENTREGADO`     | Actualiza estado del pedido → `ENTREGADO` |

---

## ⚠️ Manejo de Errores Estándar

Todos los microservicios devuelven errores en formato estandarizado:

```json
{
  "timestamp": "2026-06-29T13:15:00",
  "status":    404,
  "error":     "Not Found",
  "message":   "Pedido no encontrado con ID: 99"
}
```

| Código | Significado                                  |
|--------|----------------------------------------------|
| `200`  | OK — Consulta exitosa                        |
| `201`  | Created — Recurso creado                     |
| `204`  | No Content — Eliminación exitosa             |
| `400`  | Bad Request — Validación fallida             |
| `404`  | Not Found — Recurso no encontrado            |
| `409`  | Conflict — Duplicado (email, nombre único)   |
| `500`  | Internal Server Error — Error inesperado     |

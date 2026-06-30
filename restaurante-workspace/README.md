# 🚀 SISTEMA DE MICROSERVICIOS MULTIMÓDULO - ENTREGA FINAL

## 📦 COMPONENTES DE DISTRIBUCIÓN Y DEFENSA TÉCNICA

Utilice los siguientes enlaces externos para descargar las versiones listas para producción y visualizar la defensa del proyecto:

| Componente | Descripción | Enlace de Descarga (Nube externa) |
| :--- | :--- | :--- |
| **📦 Versión Sin Docker** <br>*(Arranque Nativo)* | Archivo `.zip` que contiene la carpeta `apps/` con los `.jar` compilados y el script `arrancar-nativo.bat` ordenado por fases. | [Descargar ZIP Nativo aquí](ENLACE_A_DRIVE_AQUÍ) |
| **🎥 Video de Defensa Técnica** <br>*(Evaluación Individual)* | Enlace directo al video explicativo donde se evidencia el funcionamiento, testing y el aporte técnico individual. **Duración ideal: 15 minutos (Máximo permitido: 18 minutos).** | [Ver Video Explicativo aquí](https://drive.google.com/file/d/1KMZFGSHe8TN04cpiwR_phMU7_99lqMrE/view?usp=drive_link) |

---

# 🍽️ Sistema de Gestión de Restaurante — Microservicios

Sistema modular para la gestión integral de un restaurante desarrollado con **Spring Boot 3.x**, **Spring Cloud (Eureka & Gateway)**, **MySQL** y **Maven Multi-Módulo**. Todo el tráfico externo se centraliza a través de un **API Gateway** en el puerto `8080`.

---

## 👥 Integrantes

| Nombre | Rol |
| :--- | :--- |
| Cristián Carvajal | Desarrollador Backend |
| Sebastián Castillo | Desarrollador Backend |

---

## 📋 Funcionalidades Implementadas

| Módulo | Descripción |
| :--- | :--- |
| 🍽️ **Gestión de Catálogo** | Administración de platos, bebidas, precios y disponibilidad del menú. |
| 📦 **Control de Stock** | Inventario de ingredientes e insumos con alertas de cantidad mínima. |
| 👤 **Gestión de Clientes** | Perfiles de comensales con datos de contacto y dirección de despacho. |
| 📅 **Sistema de Reservas** | Agendamiento de mesas, horarios y control de capacidad del local. |
| 👨‍🍳 **Cocina y Comandas** | Órdenes de preparación en tiempo real para los chefs con estados actualizables. |
| 🧾 **Orquestador de Pedidos** | Flujo completo del ciclo de vida de una orden (`CREADO → PAGADO → ENTREGADO`). |
| 💳 **Pasarela de Pagos** | Procesamiento de transacciones con métodos múltiples y disparo automático de facturación. |
| 🚗 **Logística de Despacho** | Asignación de repartidores, tiempos estimados y cierre automático del ciclo del pedido. |
| 🧾 **Facturación con IVA** | Emisión automática de boletas fiscales con folio único y cálculo de impuesto (19%). |
| 🔔 **Notificaciones** | Alertas automáticas por correo simulado al cliente al momento de crear su pedido. |
| 🌐 **Descubrimiento y Enrutamiento** | Registro dinámico en Eureka y enrutamiento inteligente por API Gateway. |

---

## 🗺️ Mapa de Puertos y Rutas

| Servicio | Puerto | Ruta Base (vía Gateway) |
| :--- | :---: | :--- |
| **api-gateway** | `8080` | `http://localhost:8080/` |
| **eureka-server** | `8761` | `http://localhost:8761` |
| **ms-catalogo** | `8081` | `/api/catalogo/platos` |
| **ms-stock** | `8082` | `/api/stock/ingredientes` |
| **ms-clientes** | `8083` | `/api/clientes` |
| **ms-reservas** | `8084` | `/api/reservas` |
| **ms-cocina** | `8085` | `/api/cocina/comandas` |
| **ms-pedidos** | `8086` | `/api/pedidos` |
| **ms-pagos** | `8087` | `/api/pagos` |
| **ms-despacho** | `8088` | `/api/despachos` |
| **ms-facturacion** | `8089` | `/api/facturacion` |
| **ms-notificaciones** | `8090` | `/api/notificaciones` |

---

## ⚙️ Puesta en Marcha — Versión Nativa

### Prerrequisitos

- ☕ Java 21+
- 🐬 XAMPP con **Apache** y **MySQL** activos (puerto `3306`)
- 📦 Maven 3.9+ (solo si deseas compilar desde fuente)

### Paso 1 — Base de Datos

1. Inicia **XAMPP** y activa los módulos **Apache** y **MySQL**.
2. Abre **phpMyAdmin** en `http://localhost/phpmyadmin`.
3. Importa el script unificado que crea las 10 bases de datos e inserta datos de prueba:

```
docs/bd-general.sql
```

### Paso 2 — Ejecución Automática con Script `.bat`

Descarga la **Versión Nativa** desde el enlace de arriba, descomprime el `.zip` y ejecuta:

```bat
arrancar-nativo.bat
```

El script levanta los servicios en el **orden jerárquico obligatorio**:

```
1. eureka-server        →  Puerto 8761  (Espera 20 s para registrarse)
2. ms-catalogo          →  Puerto 8081
   ms-stock             →  Puerto 8082
   ms-clientes          →  Puerto 8083
   ms-reservas          →  Puerto 8084
   ms-cocina            →  Puerto 8085
   ms-pedidos           →  Puerto 8086
   ms-pagos             →  Puerto 8087
   ms-despacho          →  Puerto 8088
   ms-facturacion       →  Puerto 8089
   ms-notificaciones    →  Puerto 8090  (Espera 25 s para registrarse en Eureka)
3. api-gateway          →  Puerto 8080  (Último en arrancar)
```

> ⚠️ **Importante:** Eureka debe estar completamente levantado antes de iniciar los microservicios. El API Gateway debe arrancar siempre al final para encontrar las instancias ya registradas.

### Paso 3 — Verificación

- Panel Eureka: `http://localhost:8761` — deben aparecer **11 instancias** como **UP** (api-gateway + 10 microservicios).
- Prueba rápida de endpoint vía Postman: `GET http://localhost:8080/api/clientes`

### Paso 4 — Detener el Sistema

Para finalizar todos los procesos Java activos, ejecuta:

```bat
detener-nativo.bat
```

> ⚠️ Este script cierra **todos** los procesos `java.exe`. Si tienes otras aplicaciones Java abiertas, también se cerrarán. Como alternativa, puedes cerrar manualmente cada ventana de microservicio.

---

## 🧪 Pruebas Unitarias

El proyecto incluye pruebas unitarias con **JUnit 5** y **Mockito** para las capas **Controller** y **Service** de los microservicios principales.

### Ejecutar todas las pruebas

Desde la raíz del proyecto:

```bash
mvn clean install
```

### Ejecutar pruebas de un microservicio específico

```bash
# Ejemplo: solo ms-pedidos
mvn -pl ms-pedidos test

# Ejemplo: clase específica
mvn -pl ms-clientes -Dtest=ClienteServiceTest test
```

### Compilar omitiendo pruebas

```bash
mvn clean install -DskipTests
```

---

## 📄 Documentación de Endpoints (Swagger / OpenAPI)

Cada microservicio expone su documentación interactiva de endpoints con **Springdoc OpenAPI** en:

```
http://localhost:{puerto}/swagger-ui/index.html
```

### Accesos directos

| Microservicio | URL de Swagger |
| :--- | :--- |
| ms-catalogo | http://localhost:8081/swagger-ui/index.html |
| ms-stock | http://localhost:8082/swagger-ui/index.html |
| ms-clientes | http://localhost:8083/swagger-ui/index.html |
| ms-reservas | http://localhost:8084/swagger-ui/index.html |
| ms-cocina | http://localhost:8085/swagger-ui/index.html |
| ms-pedidos | http://localhost:8086/swagger-ui/index.html |
| ms-pagos | http://localhost:8087/swagger-ui/index.html |
| ms-despacho | http://localhost:8088/swagger-ui/index.html |
| ms-facturacion | http://localhost:8089/swagger-ui/index.html |
| ms-notificaciones | http://localhost:8090/swagger-ui/index.html |

> La documentación completa del contrato de API (todos los endpoints, request/response bodies y códigos HTTP) se encuentra en [`docs/endpoints.md`](./docs/endpoints.md).

---

## 🔗 Comunicación entre Microservicios (OpenFeign)

| Servicio Origen | Servicio Destino | Acción |
| :--- | :--- | :--- |
| `ms-pedidos` | `ms-clientes` | Valida que el cliente exista antes de crear la orden. |
| `ms-pedidos` | `ms-notificaciones` | Dispara alerta automática al correo del cliente. |
| `ms-pagos` | `ms-pedidos` | Actualiza el estado del pedido a `PAGADO`. |
| `ms-pagos` | `ms-facturacion` | Emite automáticamente la factura con IVA. |
| `ms-despacho` | `ms-pedidos` | Cierra el ciclo del pedido con estado `ENTREGADO`. |

---

## 🔄 Flujo Funcional Principal (Happy Path)

Secuencia de pruebas End-to-End en Postman, apuntando al API Gateway `http://localhost:8080`:

```bash
# 1. Registrar un cliente
POST /api/clientes
Body: { "nombre": "Test User", "email": "test@email.cl", "telefono": "+56999999999", "direccion": "Calle Test 123" }

# 2. Crear un pedido (verifica cliente + envía notificación automáticamente)
POST /api/pedidos
Body: { "clienteId": 1, "tipoEntrega": "DELIVERY" }

# 3. Registrar comanda en cocina
POST /api/cocina/comandas
Body: { "pedidoId": 1, "platoId": 1, "cantidad": 2, "estado": "PENDIENTE", "notas": "Sin sal" }

# 4. Actualizar comanda a LISTO
PATCH /api/cocina/comandas/1/estado?estado=LISTO

# 5. Registrar el pago (actualiza pedido a PAGADO + emite factura automáticamente)
POST /api/pagos
Body: { "pedidoId": 1, "monto": 17000.00, "metodoPago": "EFECTIVO" }

# 6. Consultar la factura emitida
GET /api/facturacion/pedido/1

# 7. Programar el despacho
POST /api/despachos
Body: { "pedidoId": 1, "repartidorAsignado": "Juan", "direccionEntrega": "Calle Test 123", "estado": "EN_PREPARACION", "fechaEntregaEstimada": "2026-07-01T21:00:00" }

# 8. Actualizar el despacho a EN_RUTA (registra fechaSalida automáticamente)
PATCH /api/despachos/1/estado?estado=EN_RUTA

# 9. Confirmar entrega (cierra el ciclo: pedido pasa a ENTREGADO automáticamente)
PUT /api/despachos/1
Body: { "pedidoId": 1, "repartidorAsignado": "Juan", "direccionEntrega": "Calle Test 123", "estado": "ENTREGADO", "fechaEntregaEstimada": "2026-07-01T21:00:00" }

# 10. Verificar el estado final del pedido
GET /api/pedidos/1
→ estado: "ENTREGADO" ✅
```

---

## 🗂️ Estructura del Proyecto

```
restaurante-workspace/
├── pom.xml                      # POM padre (Maven Multi-Módulo)
├── README.md
├── docs/
│   ├── bd-general.sql           # Script unificado de las 10 bases de datos
│   ├── endpoints.md             # Contrato de API completo
│   └── documentacion/
│       ├── setup.md             # Guía de instalación y configuración
│       ├── variables-entorno.md # Variables de entorno del sistema
│       └── decisiones-diseno.md # Bitácora de decisiones arquitectónicas
├── eureka-server/               # Servidor de descubrimiento (puerto 8761)
├── api-gateway/                 # Puerta de entrada única (puerto 8080)
├── ms-catalogo/                 # Gestión del menú del restaurante
├── ms-stock/                    # Control de inventario de bodega
├── ms-clientes/                 # Gestión de perfiles de clientes
├── ms-reservas/                 # Agendamiento de mesas
├── ms-cocina/                   # Comandas y estado de preparación
├── ms-pedidos/                  # Orquestador del ciclo de vida del pedido
├── ms-pagos/                    # Registro de transacciones financieras
├── ms-despacho/                 # Logística de entrega a domicilio
├── ms-facturacion/              # Emisión de documentos fiscales con IVA
└── ms-notificaciones/           # Centro de alertas y notificaciones
```

---

> **Desarrollado para la asignatura Desarrollo Fullstack I** — Evaluación Parcial 3

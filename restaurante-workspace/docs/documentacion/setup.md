# ⚙️ Guía de Instalación y Configuración — Sistema Restaurante Microservicios

> **Stack tecnológico:** Java 21 · Spring Boot 3.x · Spring Cloud · MySQL 8 · Maven · XAMPP

---

## 📋 Requisitos Previos

Asegúrate de tener instalado y funcionando todo lo siguiente **antes** de ejecutar el proyecto:

| Herramienta       | Versión mínima | Verificación                         | Descarga |
|-------------------|:--------------:|--------------------------------------|----------|
| **Java (JDK)**    | 21             | `java -version`                      | [adoptium.net](https://adoptium.net) |
| **Maven**         | 3.9+           | `mvn -version`                       | [maven.apache.org](https://maven.apache.org) |
| **XAMPP**         | 8.x            | Panel de control XAMPP (MySQL)       | [apachefriends.org](https://www.apachefriends.org) |
| **Git**           | 2.x            | `git --version`                      | [git-scm.com](https://git-scm.com) |
| **IntelliJ IDEA** | 2023+          | IDE recomendado (también Eclipse/VS Code) | [jetbrains.com](https://www.jetbrains.com/idea) |
| **Postman**       | Cualquiera     | Para pruebas de la API               | [postman.com](https://www.postman.com) |

---

## 🗃️ Paso 1: Preparar la Base de Datos

### 1.1 Iniciar MySQL con XAMPP

1. Abre el panel de control de **XAMPP**.
2. Haz clic en **Start** junto a **Apache** (opcional, para phpMyAdmin).
3. Haz clic en **Start** junto a **MySQL** → debe correr en el puerto `3306`.

> ✅ Verifica que MySQL esté **verde** en el panel antes de continuar.

### 1.2 Crear los Esquemas (Bases de Datos)

Abre **phpMyAdmin** (`http://localhost/phpmyadmin`) o el cliente MySQL de tu preferencia y ejecuta el script maestro:

```bash
# Opción A — MySQL CLI
mysql -u root -p < "docs/bd-general.sql"

# Opción B — phpMyAdmin
# Ir a "Importar" → seleccionar docs/bd-general.sql → Ejecutar
```

El script crea automáticamente las 10 bases de datos y carga datos de prueba:

| Base de datos      | Servicio asociado  |
|--------------------|--------------------|
| `catalogo_db`      | ms-catalogo        |
| `stock_db`         | ms-stock           |
| `clientes_db`      | ms-clientes        |
| `reservas_db`      | ms-reservas        |
| `cocina_db`        | ms-cocina          |
| `pedidos_db`       | ms-pedidos         |
| `pagos_db`         | ms-pagos           |
| `despacho_db`      | ms-despacho        |
| `facturacion_db`   | ms-facturacion     |
| `notificaciones_db`| ms-notificaciones  |

---

## 📥 Paso 2: Clonar el Repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd restaurante-workspace
```

---

## 🔨 Paso 3: Compilar el Proyecto

Desde la raíz del workspace (donde está el `pom.xml` padre):

```bash
mvn clean install -DskipTests
```

> ⏳ La primera ejecución descarga dependencias de Maven (~3-5 minutos). Las siguientes son mucho más rápidas.

---

## 🚀 Paso 4: Levantar los Servicios en Orden

> ⚠️ **El orden es crítico.** Eureka debe estar arriba antes que cualquier otro servicio.

### Orden de arranque obligatorio:

```
1. MySQL (XAMPP)              ← Motor de base de datos
2. eureka-server  (:8761)     ← Servidor de descubrimiento
3. api-gateway    (:8080)     ← Puerta de entrada única
4. Resto de microservicios    ← Cualquier orden entre ellos
```

### Opción A — Desde IntelliJ IDEA

1. Abre el proyecto como **proyecto Maven multi-módulo**.
2. Ejecuta cada módulo en el orden indicado usando el botón ▶️ Run.
3. Espera ver en la consola: `Tomcat started on port(s): XXXX` antes de iniciar el siguiente.

### Opción B — Desde Maven CLI (una ventana por servicio)

```bash
# Terminal 1 — Eureka Server (esperar "Started" antes de continuar)
mvn spring-boot:run -pl eureka-server

# Terminal 2 — API Gateway
mvn spring-boot:run -pl api-gateway

# Terminales 3-12 — Microservicios (en cualquier orden)
mvn spring-boot:run -pl ms-catalogo
mvn spring-boot:run -pl ms-stock
mvn spring-boot:run -pl ms-clientes
mvn spring-boot:run -pl ms-reservas
mvn spring-boot:run -pl ms-cocina
mvn spring-boot:run -pl ms-pedidos
mvn spring-boot:run -pl ms-pagos
mvn spring-boot:run -pl ms-despacho
mvn spring-boot:run -pl ms-facturacion
mvn spring-boot:run -pl ms-notificaciones
```

---

## ✅ Paso 5: Verificar que Todo Esté Correcto

### 5.1 Dashboard de Eureka
Abre en tu navegador:
```
http://localhost:8761
```
Deberías ver **todos los servicios registrados** en la sección **"Instances currently registered with Eureka"**:

```
API-GATEWAY            ← 1 instancia
MS-CATALOGO            ← 1 instancia
MS-STOCK               ← 1 instancia
MS-CLIENTES            ← 1 instancia
MS-RESERVAS            ← 1 instancia
MS-COCINA              ← 1 instancia
MS-PEDIDOS             ← 1 instancia
MS-PAGOS               ← 1 instancia
MS-DESPACHO            ← 1 instancia
MS-FACTURACION         ← 1 instancia
MS-NOTIFICACIONES      ← 1 instancia
```
> Total esperado: **11 instancias** (incluyendo api-gateway).

### 5.2 Prueba rápida vía Postman

```
GET http://localhost:8080/api/clientes
→ Debería retornar: 200 OK con la lista de 5 clientes de prueba

GET http://localhost:8080/api/catalogo/platos
→ Debería retornar: 200 OK con los 10 platos del menú

GET http://localhost:8080/api/pedidos
→ Debería retornar: 200 OK con los 3 pedidos de prueba
```

---

## 🔥 Flujo de Prueba Completo (Happy Path)

Sigue esta secuencia en Postman para probar el sistema end-to-end:

```bash
# 1. Crear un cliente
POST http://localhost:8080/api/clientes
Body: { "nombre": "Test User", "email": "test@email.cl", "telefono": "+56999999999", "direccion": "Calle Test 123" }

# 2. Crear un pedido (usa el ID del cliente creado)
POST http://localhost:8080/api/pedidos
Body: { "clienteId": 6, "tipoEntrega": "DELIVERY" }
→ El sistema verifica el cliente y envía una notificación automáticamente

# 3. Registrar una comanda de cocina
POST http://localhost:8080/api/cocina/comandas
Body: { "pedidoId": 4, "platoId": 1, "cantidad": 2, "estado": "PENDIENTE", "notas": "Sin sal" }

# 4. Actualizar estado de comanda a lista
PATCH http://localhost:8080/api/cocina/comandas/5/estado?estado=LISTO

# 5. Registrar el pago
POST http://localhost:8080/api/pagos
Body: { "pedidoId": 4, "monto": 17000.00, "metodoPago": "EFECTIVO" }
→ Automáticamente: pedido pasa a PAGADO + factura emitida con IVA

# 6. Ver la factura generada
GET http://localhost:8080/api/facturacion/pedido/4

# 7. Programar despacho
POST http://localhost:8080/api/despachos
Body: { "pedidoId": 4, "repartidorAsignado": "Juan", "direccionEntrega": "Calle Test 123", "estado": "EN_PREPARACION", "fechaEntregaEstimada": "2026-06-30T21:00:00" }

# 8. Actualizar estado a en ruta
PATCH http://localhost:8080/api/despachos/2/estado?estado=EN_RUTA

# 9. Confirmar entrega (cierra el ciclo del pedido)
PUT http://localhost:8080/api/despachos/2
Body: { "pedidoId": 4, "repartidorAsignado": "Juan", "direccionEntrega": "Calle Test 123", "estado": "ENTREGADO", "fechaEntregaEstimada": "2026-06-30T21:00:00" }
→ Automáticamente: pedido pasa a ENTREGADO

# 10. Verificar estado final del pedido
GET http://localhost:8080/api/pedidos/4
→ estado: "ENTREGADO" ✅
```

---

## 🛑 Solución de Problemas Comunes

| Problema | Causa probable | Solución |
|----------|---------------|----------|
| Servicio no aparece en Eureka | Eureka no estaba arriba cuando inició el MS | Reiniciar el microservicio |
| `Connection refused` al pagar | ms-pedidos o ms-facturacion caídos | Verificar que estén arriba en Eureka |
| `Unknown host` en Feign | El nombre en `@FeignClient(name=...)` no coincide con `spring.application.name` | Revisar `application.properties` de cada servicio |
| MySQL `Access denied` | Credenciales incorrectas | Verificar `spring.datasource.username/password` en application.properties |
| `Port already in use` | Otro proceso usa el puerto | Cambiar `server.port` o matar el proceso (`netstat -ano | findstr :8081`) |
| Factura sin datos de IVA | ms-facturacion no recibió la llamada de Feign | Revisar logs de ms-pagos para errores de Feign |

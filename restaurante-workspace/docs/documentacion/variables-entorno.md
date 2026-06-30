# 🔐 Variables de Entorno — Sistema Restaurante Microservicios

> Todas las variables sensibles (credenciales, secretos, URLs) deben configurarse mediante variables de entorno o archivos `.env` — **nunca hardcodeadas en el código fuente**.

---

## 📁 Archivo `.env` de Ejemplo

Crea un archivo `.env` en la raíz del workspace y configura los valores según tu entorno local:

```bash
# ============================================================
# .env — Variables de entorno para el Sistema de Restaurante
# ============================================================
# ⚠️  NUNCA subas este archivo a Git (está en .gitignore)
# ⚠️  Para producción, usa variables de entorno reales del servidor
# ============================================================

# ─────────────────────────────────────────────
# BASE DE DATOS MYSQL (XAMPP local por defecto)
# ─────────────────────────────────────────────
DB_HOST=localhost
DB_PORT=3306
DB_USERNAME=root
DB_PASSWORD=

# ─────────────────────────────────────────────
# EUREKA SERVER
# ─────────────────────────────────────────────
EUREKA_HOST=localhost
EUREKA_PORT=8761
EUREKA_URL=http://${EUREKA_HOST}:${EUREKA_PORT}/eureka/

# ─────────────────────────────────────────────
# API GATEWAY
# ─────────────────────────────────────────────
GATEWAY_PORT=8080

# ─────────────────────────────────────────────
# PUERTOS DE MICROSERVICIOS
# ─────────────────────────────────────────────
PORT_CATALOGO=8081
PORT_STOCK=8082
PORT_CLIENTES=8083
PORT_RESERVAS=8084
PORT_COCINA=8085
PORT_PEDIDOS=8086
PORT_PAGOS=8087
PORT_DESPACHO=8088
PORT_FACTURACION=8089
PORT_NOTIFICACIONES=8090

# ─────────────────────────────────────────────
# CONFIGURACIÓN JPA / HIBERNATE
# ─────────────────────────────────────────────
# update = actualiza esquema en caliente (recomendado en desarrollo)
# create = DESTRUYE y recrea todas las tablas en cada arranque
# validate = solo valida que el esquema coincida
# none = no toca el esquema (recomendado en producción)
JPA_DDL_AUTO=update
JPA_SHOW_SQL=true

# ─────────────────────────────────────────────
# FACTURACIÓN — TASA IMPOSITIVA
# ─────────────────────────────────────────────
# IVA chileno estándar = 19%
IMPUESTO_PORCENTAJE=0.19

# ─────────────────────────────────────────────
# NOTIFICACIONES — SMTP (Email simulado)
# ─────────────────────────────────────────────
# Para pruebas locales usa Mailtrap.io (servidor SMTP falso)
MAIL_HOST=smtp.mailtrap.io
MAIL_PORT=587
MAIL_USERNAME=tu_usuario_mailtrap
MAIL_PASSWORD=tu_password_mailtrap
MAIL_FROM=noreply@restaurante.cl

# ─────────────────────────────────────────────
# SEGURIDAD (para implementación futura con JWT)
# ─────────────────────────────────────────────
JWT_SECRET=reemplazar_con_clave_secreta_larga_y_aleatoria_min_256_bits
JWT_EXPIRATION_MS=86400000
```

---

## 📄 Referencia por Microservicio

### `application.properties` tipo para cada microservicio

```properties
# ── Identidad del servicio ──────────────────────────────────
spring.application.name=ms-nombre-del-servicio
server.port=${PORT_SERVICIO:808X}

# ── Base de datos propia ────────────────────────────────────
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/nombre_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Santiago
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── JPA / Hibernate ─────────────────────────────────────────
spring.jpa.hibernate.ddl-auto=${JPA_DDL_AUTO:update}
spring.jpa.show-sql=${JPA_SHOW_SQL:true}
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# ── Eureka Client ───────────────────────────────────────────
eureka.client.service-url.defaultZone=${EUREKA_URL:http://localhost:8761/eureka/}
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
eureka.instance.prefer-ip-address=true
```

---

## 🗂️ Variables por Servicio

| Variable                           | Servicio(s)           | Descripción                                     | Valor por defecto |
|------------------------------------|-----------------------|-------------------------------------------------|-------------------|
| `DB_HOST`                          | Todos los MS          | Host de MySQL                                   | `localhost`       |
| `DB_PORT`                          | Todos los MS          | Puerto de MySQL                                 | `3306`            |
| `DB_USERNAME`                      | Todos los MS          | Usuario de MySQL                                | `root`            |
| `DB_PASSWORD`                      | Todos los MS          | Contraseña de MySQL                             | `""` (vacía)      |
| `EUREKA_URL`                       | Todos los MS          | URL de registro en Eureka                       | `http://localhost:8761/eureka/` |
| `PORT_CATALOGO`                    | ms-catalogo           | Puerto del servicio de catálogo                 | `8081`            |
| `PORT_STOCK`                       | ms-stock              | Puerto del servicio de stock                    | `8082`            |
| `PORT_CLIENTES`                    | ms-clientes           | Puerto del servicio de clientes                 | `8083`            |
| `PORT_RESERVAS`                    | ms-reservas           | Puerto del servicio de reservas                 | `8084`            |
| `PORT_COCINA`                      | ms-cocina             | Puerto del servicio de cocina                   | `8085`            |
| `PORT_PEDIDOS`                     | ms-pedidos            | Puerto del orquestador de pedidos               | `8086`            |
| `PORT_PAGOS`                       | ms-pagos              | Puerto del servicio de pagos                    | `8087`            |
| `PORT_DESPACHO`                    | ms-despacho           | Puerto del servicio de despacho                 | `8088`            |
| `PORT_FACTURACION`                 | ms-facturacion        | Puerto del servicio de facturación              | `8089`            |
| `PORT_NOTIFICACIONES`              | ms-notificaciones     | Puerto del servicio de notificaciones           | `8090`            |
| `JPA_DDL_AUTO`                     | Todos los MS          | Estrategia de Hibernate para el esquema         | `update`          |
| `JPA_SHOW_SQL`                     | Todos los MS          | Mostrar queries SQL en consola                  | `true`            |
| `IMPUESTO_PORCENTAJE`              | ms-facturacion        | Tasa IVA aplicada a las facturas                | `0.19`            |
| `MAIL_HOST`                        | ms-notificaciones     | Servidor SMTP para envío de emails              | `smtp.mailtrap.io`|
| `MAIL_PORT`                        | ms-notificaciones     | Puerto SMTP                                     | `587`             |
| `MAIL_USERNAME`                    | ms-notificaciones     | Usuario del servidor SMTP                       | —                 |
| `MAIL_PASSWORD`                    | ms-notificaciones     | Contraseña del servidor SMTP                    | —                 |
| `JWT_SECRET`                       | Futuro (seguridad)    | Clave secreta para firmar tokens JWT            | —                 |
| `JWT_EXPIRATION_MS`                | Futuro (seguridad)    | Duración del token JWT en milisegundos          | `86400000` (24h)  |

---

## 🔒 Buenas Prácticas de Seguridad

> [!CAUTION]
> Nunca hagas commit de credenciales reales al repositorio.

1. **Agrega `.env` al `.gitignore`:**
   ```gitignore
   .env
   *.env
   application-prod.properties
   ```

2. **Usa valores por defecto seguros en el código:**
   ```properties
   # Forma correcta: variable de entorno con valor fallback
   spring.datasource.password=${DB_PASSWORD:}
   ```

3. **Para producción:** usa un gestor de secretos como:
   - **AWS Secrets Manager**
   - **HashiCorp Vault**
   - **Spring Cloud Config Server** con repositorio privado

4. **Rota las credenciales** periódicamente en producción.

5. **Nunca loguees** variables sensibles (`log.info("Password: {}", password)` ❌).

---

## 🌐 Ambientes

| Ambiente     | `DB_HOST`       | `JPA_DDL_AUTO` | `JPA_SHOW_SQL` | Notas |
|--------------|-----------------|----------------|----------------|-------|
| **Local**    | `localhost`     | `update`       | `true`         | XAMPP |
| **Testing**  | `localhost`     | `create-drop`  | `true`         | H2 en memoria (opcional) |
| **Staging**  | IP servidor     | `validate`     | `false`        | MySQL en servidor |
| **Producción**| IP servidor    | `none`         | `false`        | Migraciones con Flyway/Liquibase |

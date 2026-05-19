# Sistema de Gestión de Restaurante Basado en Microservicios

Este proyecto consiste en un ecosistema completo y automatizado para la gestión integral de un restaurante. Está diseñado bajo una arquitectura de microservicios utilizando **Spring Boot**, **Spring Cloud (Eureka y API Gateway)**, **MySQL** y comunicación sincrónica mediante **OpenFeign**. El sistema cubre desde la consulta del catálogo de platos hasta la notificación final al cliente y el despacho del pedido.

---

## Integrantes
* [Cristián Carvajal]
* [Sebastián Castillo]

---

## Funcionalidades Implementadas

1. **Registro y Validación de Clientes:** Verificación automática de la existencia de un cliente antes de permitir la creación de cualquier pedido.
2. **Gestión Operativa de Pedidos:** Flujo completo del ciclo de vida de una orden (Creado, Pagado, Entregado).
3. **Automatización de Pagos y Facturación:** Al registrar un pago exitoso, el sistema actualiza automáticamente el estado del pedido y acciona la emisión de un documento con cálculo automático de impuestos (19% IVA).
4. **Sincronización Logística (Despacho):** Al momento en que el repartidor marca un despacho como "ENTREGADO", el sistema cierra automáticamente el ciclo del pedido en el orquestador y registra la fecha/hora exacta de entrega.
5. **Notificaciones Automatizadas:** Integración para el envío de alertas por correo electrónico simulado al cliente en cuanto su pedido es recibido con éxito.
6. **Descubrimiento y Enrutamiento Dinámico:** Centralización de todos los servicios a través de un servidor de descubrimiento y un punto único de entrada para el cliente (API Gateway).

---

## Microservicios del Proyecto

| Nombre del Microservicio | Puerto Interno | URL de Ejecución (Postman a través del Gateway) | Breve Descripción |
| :--- | :---: | :--- | :--- |
| **eureka-server** | `8761` | `http://localhost:8761` | Servidor donde se registran automáticamente todos los servicios. |
| **api-gateway** | `8080` | `http://localhost:8080` | Puerta de entrada única del sistema. Enruta las peticiones de Postman al microservicio correcto. |
| **ms-catalogo** | `8081` | `http://localhost:8080/api/catalogo/platos` | Administra la lista de platos, bebidas, descripciones y precios disponibles en el menú. |
| **ms-stock** | `8082` | `http://localhost:8080/api/stock/ingredientes` | Controla los ingredientes y existencias físicas disponibles en la bodega del restaurante. |
| **ms-clientes** | `8083` | `http://localhost:8080/api/clientes` | Gestiona los perfiles, datos de contacto y correos electrónicos de los comensales. |
| **ms-reservas** | `8084` | `http://localhost:8080/api/reservas` | Gestiona el agendamiento de mesas y horarios en el restaurante. |
| **ms-cocina** | `8085` | `http://localhost:8080/api/cocina/comandas` | Monitorea la preparación de los platos y las comandas que deben elaborar los chefs. |
| **ms-pedidos** | `8086` | `http://localhost:8080/api/pedidos` | Orquestador central que maneja el ciclo de vida y los estados de las órdenes de comida. |
| **ms-pagos** | `8087` | `http://localhost:8080/api/pagos` | Procesa las transacciones financieras y métodos de pago (Efectivo, Tarjeta, etc.). |
| **ms-despacho** | `8088` | `http://localhost:8080/api/despachos` | Administra la asignación de repartidores, direcciones y el estado de los envíos a domicilio. |
| **ms-facturacion** | `8089` | `http://localhost:8080/api/facturacion` | Genera los comprobantes y calcula de forma automática los impuestos aplicados. |
| **ms-notificaciones** | `8090` | `http://localhost:8080/api/notificaciones` | Se encarga de enviar alertas y correos automáticos de confirmación a los clientes. |

---

## Pasos para Ejecutar el Proyecto

Sigue detalladamente este orden para levantar todo el ecosistema de forma correcta:

### Paso 1: Activar la Base de Datos (XAMPP)
1. Abre el panel de control de **XAMPP**.
2. Haz clic en el botón **Start** de **Apache**.
3. Haz clic en el botón **Start** de **MySQL** (asegúrate de que corra en su puerto estándar `3306`).

### Paso 2: Ejecutar la Infraestructura en tu Editor de Código (IDE)
Abre tu entorno de desarrollo (ej: IntelliJ IDEA, Eclipse o VS Code) e inicia los servicios en el siguiente orden (esperando unos segundos entre cada uno para permitir que se levanten por completo):
1. **Primero:** Inicia `eureka-server` (Verifica que esté listo entrando a `http://localhost:8761` en tu navegador).
2. **Segundo:** Inicia `api-gateway`.
3. **Tercero (Resto de Microservicios):** Levanta los 10 servicios de negocio en cualquier orden (`ms-pedidos`, `ms-clientes`, `ms-catalogo`, `ms-stock`, `ms-reservas`, `ms-cocina`, `ms-pagos`, `ms-despacho`, `ms-facturacion`, `ms-notificaciones`). 
4. *Nota:* Revisa la consola de Eureka (`http://localhost:8761`) y asegúrate de que los 11 servicios (incluyendo api-gateway) aparezcan listados en la sección de aplicaciones activas.

### Paso 3: Realizar Peticiones en Postman
No necesitas llamar a los puertos internos de cada servicio. El API Gateway unifica todo bajo el puerto **8080**.
1. Abre **Postman**.
2. Todas tus peticiones deben apuntar a la dirección base: `http://localhost:8080`.
3. Ejemplos de endpoints listos para pruebas:
   * **Crear Pedido (POST):** `http://localhost:8080/api/pedidos`
   * **Registrar Pago (POST):** `http://localhost:8080/api/pagos`
   * **Actualizar Despacho (PUT):** `http://localhost:8080/api/despachos/1`
   * **Ver Facturas Emitidas (GET):** `http://localhost:8080/api/facturacion`

---

**Link video del proyecto:** https://drive.google.com/file/d/1KMZFGSHe8TN04cpiwR_phMU7_99lqMrE/view?usp=drive_link

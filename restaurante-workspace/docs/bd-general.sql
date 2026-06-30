-- =============================================================================
--  PLANO MAESTRO DE DATOS — SISTEMA DE GESTIÓN DE RESTAURANTE
--  Arquitectura: Database-per-Service (Microservices Pattern)
--  Motor:        MySQL 8.x  (ejecutado con XAMPP / localhost:3306)
--  Autor:        Cristián Carvajal | Sebastián Castillo
--  Fecha:        2026-06-29
--
--  ORDEN DE EJECUCIÓN RECOMENDADO:
--    1. Ejecutar TODO este script en MySQL Workbench / phpMyAdmin / CLI.
--    2. Levantar los microservicios (Spring Boot creará las tablas con Hibernate).
--       Este script sirve como referencia exacta del esquema y como semilla de datos.
--
--  NOTA SOBRE HIBERNATE (spring.jpa.hibernate.ddl-auto):
--    Si los servicios tienen "update" o "create", Hibernate gestiona las tablas.
--    Este script es el plano maestro para:
--       a) Crear las bases de datos vacías antes de levantar los servicios.
--       b) Insertar datos de prueba (seed data) tras el primer arranque.
-- =============================================================================

-- =============================================================================
-- PASO 1: CREACIÓN DE LAS BASES DE DATOS INDIVIDUALES
-- Cada microservicio tiene su propio esquema — patrón Database per Service.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS catalogo_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stock_db      CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS clientes_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS reservas_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS cocina_db    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pedidos_db   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pagos_db     CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS despacho_db  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS facturacion_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS notificaciones_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;


-- =============================================================================
-- [ms-catalogo]  Puerto: 8081  |  Base de datos: catalogo_db
-- Gestiona el menú del restaurante: platos, bebidas y precios.
-- =============================================================================
USE catalogo_db;

CREATE TABLE IF NOT EXISTS platos (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(100)    NOT NULL UNIQUE COMMENT 'Nombre único del plato o bebida en el menú',
    descripcion VARCHAR(255)    NOT NULL COMMENT 'Descripción del plato para el cliente',
    precio      DOUBLE          NOT NULL COMMENT 'Precio en moneda local (CLP)',
    disponible  TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '1 = disponible para ordenar, 0 = agotado',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Menú del restaurante — administrado por ms-catalogo';

-- Seed Data: Menú base del restaurante
INSERT INTO platos (nombre, descripcion, precio, disponible) VALUES
    ('Lomo Saltado',       'Clásico salteado de lomo, tomate, cebolla y papas fritas',    8500.00, 1),
    ('Pastel de Choclo',   'Pastel tradicional chileno con pino de carne y choclo molido', 7200.00, 1),
    ('Cazuela de Vacuno',  'Sopa contundente con trozo de vacuno, papas y choclo',         6800.00, 1),
    ('Empanada Frita',     'Empanada de pino frita, rellena con carne, aceitunas y huevo', 2500.00, 1),
    ('Ensalada Chilena',   'Tomate y cebolla picados en aceite y limón',                   3200.00, 1),
    ('Bebida en Lata',     'Coca-Cola, Pepsi o Fanta (350ml)',                             1500.00, 1),
    ('Jugo Natural',       'Jugo de naranja, mango o durazno recién exprimido (400ml)',    2200.00, 1),
    ('Sopaipilla Pasada',  'Sopaipilla bañada en chancaca con canela',                    1800.00, 1),
    ('Salmón a la Plancha','Filete de salmón con papas al vapor y ensalada verde',        12500.00, 1),
    ('Plateada al Horno',  'Corte de vacuno braseado lentamente con verduras asadas',     11000.00, 1);


-- =============================================================================
-- [ms-stock]  Puerto: 8082  |  Base de datos: stock_db
-- Inventario de ingredientes y materias primas de la bodega.
-- =============================================================================
USE stock_db;

CREATE TABLE IF NOT EXISTS ingredientes (
    id               BIGINT          NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(100)    NOT NULL UNIQUE COMMENT 'Nombre del ingrediente o insumo',
    cantidad_actual  DOUBLE          NOT NULL COMMENT 'Cantidad disponible en bodega',
    cantidad_minima  DOUBLE          NOT NULL COMMENT 'Umbral mínimo antes de alertar reposición',
    unidad_medida    VARCHAR(50)     NOT NULL COMMENT 'Unidad: Gramos, Kilogramos, Litros, Unidades',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Inventario de bodega — administrado por ms-stock';

-- Seed Data: Stock inicial de la cocina
INSERT INTO ingredientes (nombre, cantidad_actual, cantidad_minima, unidad_medida) VALUES
    ('Lomo de Vacuno',    5000.00, 1000.00, 'Gramos'),
    ('Choclo Molido',     3000.00,  500.00, 'Gramos'),
    ('Pino de Carne',     4000.00,  800.00, 'Gramos'),
    ('Harina',           10000.00, 2000.00, 'Gramos'),
    ('Aceite Vegetal',    5000.00, 1000.00, 'Mililitros'),
    ('Tomate',           30.00,     10.00, 'Unidades'),
    ('Cebolla',          25.00,      8.00, 'Unidades'),
    ('Sal',              2000.00,   500.00, 'Gramos'),
    ('Salmón Fresco',    4000.00,  500.00, 'Gramos'),
    ('Papas',            8000.00, 1500.00, 'Gramos'),
    ('Naranja',          40.00,    10.00, 'Unidades'),
    ('Bebida en Lata',   48.00,    12.00, 'Unidades');


-- =============================================================================
-- [ms-clientes]  Puerto: 8083  |  Base de datos: clientes_db
-- Perfiles, datos de contacto y correos de los comensales.
-- =============================================================================
USE clientes_db;

CREATE TABLE IF NOT EXISTS clientes (
    id        BIGINT          NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100)    NOT NULL COMMENT 'Nombre completo del cliente',
    email     VARCHAR(100)    NOT NULL UNIQUE COMMENT 'Correo único — usado para notificaciones',
    telefono  VARCHAR(15)     COMMENT 'Teléfono de contacto (opcional)',
    direccion VARCHAR(255)    NOT NULL COMMENT 'Dirección de despacho principal',
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='Directorio de clientes — administrado por ms-clientes';

-- Seed Data: Clientes de prueba
INSERT INTO clientes (nombre, email, telefono, direccion) VALUES
    ('Cristián Carvajal',  'cristian.carvajal@email.cl', '+56912345678', 'Av. Providencia 1234, Santiago'),
    ('Sebastián Castillo', 'sebastian.castillo@email.cl','+56987654321', 'Calle Larga 567, Valparaíso'),
    ('María González',     'maria.gonzalez@email.cl',    '+56911223344', 'Los Dominicos 890, Las Condes'),
    ('Pedro Soto',         'pedro.soto@email.cl',         NULL,          'Villa Olímpica 321, Ñuñoa'),
    ('Carmen López',       'carmen.lopez@email.cl',       '+56966778899', 'Camino Real 45, Maipú');


-- =============================================================================
-- [ms-reservas]  Puerto: 8084  |  Base de datos: reservas_db
-- Agendamiento de mesas y horarios en el restaurante.
-- =============================================================================
USE reservas_db;

CREATE TABLE IF NOT EXISTS reservas (
    id                BIGINT          NOT NULL AUTO_INCREMENT,
    cliente_id        BIGINT          NOT NULL COMMENT 'ID lógico del cliente en clientes_db (sin FK física)',
    fecha_hora        DATETIME        NOT NULL COMMENT 'Fecha y hora de la reserva agendada',
    cantidad_personas INT             NOT NULL COMMENT 'Número de comensales para la mesa',
    estado            VARCHAR(50)     NOT NULL DEFAULT 'PENDIENTE'
                                      COMMENT 'Estados: PENDIENTE, CONFIRMADA, CANCELADA',
    observaciones     VARCHAR(255)    COMMENT 'Notas especiales: alergias, cumpleaños, etc.',
    PRIMARY KEY (id),
    INDEX idx_reservas_cliente (cliente_id),
    INDEX idx_reservas_fecha (fecha_hora)
) ENGINE=InnoDB COMMENT='Agenda de mesas — administrado por ms-reservas';

-- Seed Data: Reservas de ejemplo
INSERT INTO reservas (cliente_id, fecha_hora, cantidad_personas, estado, observaciones) VALUES
    (1, '2026-07-01 13:00:00', 2, 'CONFIRMADA', 'Mesa con vista al jardín'),
    (2, '2026-07-01 20:00:00', 4, 'PENDIENTE',  'Celebración de cumpleaños, traer postre'),
    (3, '2026-07-02 13:30:00', 3, 'CONFIRMADA', NULL),
    (4, '2026-07-03 21:00:00', 6, 'PENDIENTE',  'Cena de empresa, menú vegetariano para 2');


-- =============================================================================
-- [ms-cocina]  Puerto: 8085  |  Base de datos: cocina_db
-- Comandas de cocina: qué preparar y su estado de elaboración.
-- =============================================================================
USE cocina_db;

CREATE TABLE IF NOT EXISTS comandas (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id BIGINT       NOT NULL COMMENT 'ID del pedido en pedidos_db al que pertenece la comanda',
    plato_id  BIGINT       NOT NULL COMMENT 'ID del plato en catalogo_db a preparar',
    cantidad  INT          NOT NULL DEFAULT 1 COMMENT 'Cantidad de porciones del plato',
    estado    VARCHAR(50)  NOT NULL DEFAULT 'PENDIENTE'
                           COMMENT 'Estados: PENDIENTE, EN_PREPARACION, LISTO',
    notas     VARCHAR(255) COMMENT 'Modificaciones del cliente: sin cebolla, término medio, etc.',
    PRIMARY KEY (id),
    INDEX idx_comandas_pedido (pedido_id),
    INDEX idx_comandas_plato  (plato_id)
) ENGINE=InnoDB COMMENT='Órdenes de cocina — administrado por ms-cocina';

-- Seed Data: Comandas del pedido de prueba #1
INSERT INTO comandas (pedido_id, plato_id, cantidad, estado, notas) VALUES
    (1, 1, 1, 'LISTO',          'Sin tomate'),
    (1, 6, 2, 'LISTO',          NULL),
    (2, 3, 1, 'EN_PREPARACION', 'Extra choclo'),
    (2, 7, 1, 'PENDIENTE',      NULL);


-- =============================================================================
-- [ms-pedidos]  Puerto: 8086  |  Base de datos: pedidos_db
-- Orquestador central del ciclo de vida de las órdenes.
-- =============================================================================
USE pedidos_db;

CREATE TABLE IF NOT EXISTS pedidos (
    id           BIGINT          NOT NULL AUTO_INCREMENT,
    cliente_id   BIGINT          NOT NULL COMMENT 'ID lógico del cliente verificado en clientes_db',
    fecha_pedido DATETIME        NOT NULL COMMENT 'Fecha y hora exacta de creación del pedido',
    estado       VARCHAR(50)     NOT NULL DEFAULT 'CREADO'
                                 COMMENT 'Ciclo de vida: CREADO → PAGADO → ENTREGADO | CANCELADO',
    tipo_entrega VARCHAR(50)     NOT NULL COMMENT 'Modalidad: MESA, DELIVERY, PARA_LLEVAR',
    total        DOUBLE          NOT NULL DEFAULT 0.0 COMMENT 'Total acumulado del pedido (calculado en el sistema)',
    PRIMARY KEY (id),
    INDEX idx_pedidos_cliente (cliente_id),
    INDEX idx_pedidos_estado  (estado)
) ENGINE=InnoDB COMMENT='Orquestador de pedidos — administrado por ms-pedidos';

-- Seed Data: Pedidos de prueba
INSERT INTO pedidos (cliente_id, fecha_pedido, estado, tipo_entrega, total) VALUES
    (1, '2026-06-29 13:15:00', 'ENTREGADO', 'DELIVERY',    10000.00),
    (2, '2026-06-29 20:30:00', 'PAGADO',    'MESA',         7200.00),
    (3, '2026-06-30 12:00:00', 'CREADO',    'PARA_LLEVAR',  6800.00);


-- =============================================================================
-- [ms-pagos]  Puerto: 8087  |  Base de datos: pagos_db
-- Registro de transacciones financieras.
-- Al registrar un pago APROBADO, llama (Feign) a ms-pedidos y ms-facturacion.
-- =============================================================================
USE pagos_db;

CREATE TABLE IF NOT EXISTS pagos (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id   BIGINT       NOT NULL COMMENT 'ID del pedido en pedidos_db que está siendo pagado',
    monto       DOUBLE       NOT NULL COMMENT 'Monto total de la transacción en CLP',
    metodo_pago VARCHAR(50)  NOT NULL COMMENT 'Método: EFECTIVO, TARJETA_CREDITO, DEBITO, TRANSFERENCIA',
    fecha_pago  DATETIME     NOT NULL COMMENT 'Marca temporal de la transacción (generada por el sistema)',
    estado      VARCHAR(50)  NOT NULL DEFAULT 'APROBADO'
                             COMMENT 'Estados: APROBADO, RECHAZADO, REEMBOLSADO',
    PRIMARY KEY (id),
    INDEX idx_pagos_pedido  (pedido_id),
    INDEX idx_pagos_estado  (estado)
) ENGINE=InnoDB COMMENT='Registro de pagos — administrado por ms-pagos';

-- Seed Data: Pagos de ejemplo
INSERT INTO pagos (pedido_id, monto, metodo_pago, fecha_pago, estado) VALUES
    (1, 10000.00, 'TARJETA_CREDITO', '2026-06-29 13:20:00', 'APROBADO'),
    (2,  7200.00, 'EFECTIVO',        '2026-06-29 20:45:00', 'APROBADO');


-- =============================================================================
-- [ms-despacho]  Puerto: 8088  |  Base de datos: despacho_db
-- Logística de entrega a domicilio.
-- Al marcar estado ENTREGADO, llama (Feign) a ms-pedidos para cerrar la orden.
-- =============================================================================
USE despacho_db;

CREATE TABLE IF NOT EXISTS despachos (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    pedido_id               BIGINT       NOT NULL COMMENT 'ID del pedido en pedidos_db a despachar',
    repartidor_asignado     VARCHAR(100) NOT NULL COMMENT 'Nombre del repartidor o transportista',
    direccion_entrega       VARCHAR(255) NOT NULL COMMENT 'Dirección exacta de entrega del pedido',
    estado                  VARCHAR(50)  NOT NULL DEFAULT 'EN_PREPARACION'
                                         COMMENT 'Flujo: EN_PREPARACION → EN_RUTA → ENTREGADO | RECHAZADO',
    fecha_salida            DATETIME     COMMENT 'Instante en que el repartidor sale del local (se marca al pasar a EN_RUTA)',
    fecha_entrega_estimada  DATETIME     COMMENT 'Hora estimada de llegada al cliente',
    fecha_entrega           DATETIME     COMMENT 'Instante real de confirmación de entrega',
    PRIMARY KEY (id),
    INDEX idx_despacho_pedido (pedido_id),
    INDEX idx_despacho_estado (estado)
) ENGINE=InnoDB COMMENT='Logística de despacho — administrado por ms-despacho';

-- Seed Data: Despacho del pedido #1 (ya entregado)
INSERT INTO despachos (pedido_id, repartidor_asignado, direccion_entrega, estado, fecha_salida, fecha_entrega_estimada, fecha_entrega) VALUES
    (1, 'Carlos Repartidor', 'Av. Providencia 1234, Santiago', 'ENTREGADO',
     '2026-06-29 13:25:00', '2026-06-29 13:50:00', '2026-06-29 13:48:00');


-- =============================================================================
-- [ms-facturacion]  Puerto: 8089  |  Base de datos: facturacion_db
-- Emisión automática de boletas/facturas con IVA calculado (19%).
-- Invocado automáticamente por ms-pagos al registrar un pago exitoso.
-- =============================================================================
USE facturacion_db;

CREATE TABLE IF NOT EXISTS facturas (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    pedido_id      BIGINT        NOT NULL COMMENT 'ID del pedido que genera este documento fiscal',
    numero_factura VARCHAR(50)   NOT NULL UNIQUE COMMENT 'Folio único fiscal (Ej: FAC-A1B2C3D4)',
    subtotal       DOUBLE        NOT NULL COMMENT 'Monto sin impuestos (igual al monto pagado)',
    impuestos      DOUBLE        NOT NULL COMMENT 'Monto del IVA calculado (subtotal * 0.19)',
    total          DOUBLE        NOT NULL COMMENT 'Total con IVA incluido (subtotal + impuestos)',
    fecha_emision  DATETIME      NOT NULL COMMENT 'Fecha y hora de emisión del documento',
    estado_fiscal  VARCHAR(50)   NOT NULL DEFAULT 'EMITIDA'
                                 COMMENT 'Estados: EMITIDA, ANULADA, RECHAZADA_POR_ENTE',
    url_pdf        VARCHAR(255)  COMMENT 'Ruta simulada al documento PDF generado',
    PRIMARY KEY (id),
    INDEX idx_facturas_pedido (pedido_id)
) ENGINE=InnoDB COMMENT='Documentos tributarios — administrado por ms-facturacion';

-- Seed Data: Factura generada automáticamente por el pago del pedido #1
INSERT INTO facturas (pedido_id, numero_factura, subtotal, impuestos, total, fecha_emision, estado_fiscal, url_pdf) VALUES
    (1, 'FAC-A1B2C3D4', 10000.00, 1900.00, 11900.00, '2026-06-29 13:21:00', 'EMITIDA',
     'https://restaurante.com/facturas/FAC-A1B2C3D4.pdf');


-- =============================================================================
-- [ms-notificaciones]  Puerto: 8090  |  Base de datos: notificaciones_db
-- Historial de alertas enviadas a los clientes (email, SMS, push).
-- Invocado por ms-pedidos al crear un nuevo pedido.
-- =============================================================================
USE notificaciones_db;

CREATE TABLE IF NOT EXISTS notificaciones (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    cliente_id     BIGINT        NOT NULL COMMENT 'ID del cliente en clientes_db al que va dirigida',
    destinatario   VARCHAR(150)  NOT NULL COMMENT 'Email o número telefónico del destinatario',
    tipo           VARCHAR(50)   NOT NULL COMMENT 'Canal: EMAIL, SMS, PUSH',
    asunto         VARCHAR(150)  COMMENT 'Asunto del correo electrónico (solo aplica para EMAIL)',
    mensaje        TEXT          NOT NULL COMMENT 'Cuerpo completo del mensaje enviado',
    estado         VARCHAR(50)   NOT NULL DEFAULT 'PENDIENTE'
                                 COMMENT 'Estados: PENDIENTE, ENVIADO, FALLIDO',
    fecha_creacion DATETIME      NOT NULL COMMENT 'Momento en que el sistema registró la notificación',
    fecha_envio    DATETIME      COMMENT 'Momento real del envío exitoso al destinatario',
    PRIMARY KEY (id),
    INDEX idx_notificaciones_cliente (cliente_id),
    INDEX idx_notificaciones_estado  (estado)
) ENGINE=InnoDB COMMENT='Historial de notificaciones — administrado por ms-notificaciones';

-- Seed Data: Notificación de confirmación del pedido #1
INSERT INTO notificaciones (cliente_id, destinatario, tipo, asunto, mensaje, estado, fecha_creacion, fecha_envio) VALUES
    (1, 'cristian.carvajal@email.cl', 'EMAIL', '¡Pedido Recibido!',
     'Hola Cristián Carvajal, hemos recibido tu pedido #1. Lo estamos preparando.',
     'ENVIADO', '2026-06-29 13:15:30', '2026-06-29 13:15:32'),
    (2, 'sebastian.castillo@email.cl', 'EMAIL', '¡Pedido Recibido!',
     'Hola Sebastián Castillo, hemos recibido tu pedido #2. Lo estamos preparando.',
     'ENVIADO', '2026-06-29 20:30:10', '2026-06-29 20:30:12');


-- =============================================================================
-- FIN DEL SCRIPT MAESTRO
-- Verificación rápida: ejecuta las siguientes consultas para confirmar la carga.
-- =============================================================================

/*
-- Verificar bases de datos creadas:
SHOW DATABASES;

-- Verificar datos de prueba:
USE catalogo_db;   SELECT COUNT(*) AS platos_totales        FROM platos;
USE stock_db;      SELECT COUNT(*) AS ingredientes_en_bodega FROM ingredientes;
USE clientes_db;   SELECT COUNT(*) AS clientes_registrados   FROM clientes;
USE reservas_db;   SELECT COUNT(*) AS reservas_activas       FROM reservas;
USE pedidos_db;    SELECT COUNT(*) AS pedidos_totales        FROM pedidos;
USE pagos_db;      SELECT COUNT(*) AS transacciones          FROM pagos;
USE despacho_db;   SELECT COUNT(*) AS despachos              FROM despachos;
USE facturacion_db;SELECT COUNT(*) AS facturas_emitidas      FROM facturas;
USE notificaciones_db; SELECT COUNT(*) AS notificaciones     FROM notificaciones;
*/

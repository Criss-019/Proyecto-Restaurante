# 🔄 Diagrama de Secuencia — Flujo Completo de un Pedido

> Muestra la interacción cronológica entre todos los microservicios desde que un cliente hace un pedido hasta que lo recibe en su domicilio.  
> Generado con **Mermaid Sequence Diagram** syntax.

---

## Flujo 1: Creación de Pedido (con Notificación Automática)

```mermaid
sequenceDiagram
    autonumber
    actor       Cliente
    participant GW  as api-gateway<br/>:8080
    participant PED as ms-pedidos<br/>:8086
    participant CLI as ms-clientes<br/>:8083
    participant NOT as ms-notificaciones<br/>:8090

    Cliente ->>+  GW  : POST /api/pedidos<br/>{ clienteId: 1, tipoEntrega: "DELIVERY" }
    GW      ->>+  PED : Enruta la petición

    Note over PED: Inicia creación del pedido

    PED     ->>+  CLI : [Feign] GET /api/clientes/1<br/>Verifica existencia del cliente
    CLI     -->>- PED : 200 OK → { id:1, nombre:"Cristián", email:"..." }

    Note over PED: Cliente verificado ✅<br/>Guarda pedido con estado="CREADO"

    PED     ->>+  NOT : [Feign] POST /api/notificaciones<br/>{ destinatario: "cristian@...", asunto: "¡Pedido Recibido!", ... }
    NOT     -->>- PED : 201 Created → Notificación ENVIADA

    PED     -->>- GW  : 201 Created → { id:1, estado:"CREADO", total:0.0, ... }
    GW      -->>  Cliente : 201 Created ✅ Pedido confirmado

    Note over Cliente: Recibe email de confirmación 📧
```

---

## Flujo 2: Registro de Pago (con Actualización de Pedido y Facturación)

```mermaid
sequenceDiagram
    autonumber
    actor       Cliente
    participant GW  as api-gateway<br/>:8080
    participant PAG as ms-pagos<br/>:8087
    participant PED as ms-pedidos<br/>:8086
    participant FAC as ms-facturacion<br/>:8089

    Cliente ->>+  GW  : POST /api/pagos<br/>{ pedidoId: 1, monto: 10000, metodoPago: "TARJETA_CREDITO" }
    GW      ->>+  PAG : Enruta la petición

    Note over PAG: Registra el pago con estado="APROBADO"<br/>fechaPago = NOW()

    PAG     ->>+  PED : [Feign] PUT /api/pedidos/1/estado?estado=PAGADO<br/>Actualiza el pedido como pagado
    PED     -->>- PAG : 200 OK → Pedido actualizado a PAGADO ✅

    PAG     ->>+  FAC : [Feign] POST /api/facturacion<br/>{ pedidoId: 1, subtotal: 10000 }
    Note over FAC: Calcula IVA:<br/>impuestos = 10000 × 0.19 = 1900<br/>total = 11900<br/>Genera folio FAC-XXXXXXXX
    FAC     -->>- PAG : 201 Created → Factura EMITIDA ✅

    PAG     -->>- GW  : 201 Created → { id:1, estado:"APROBADO", monto:10000, ... }
    GW      -->>  Cliente : 201 Created ✅ Pago aprobado

    Note over Cliente: Factura disponible en /api/facturacion/pedido/1 🧾
```

---

## Flujo 3: Despacho a Domicilio (Cierre del Ciclo del Pedido)

```mermaid
sequenceDiagram
    autonumber
    actor       Operador
    participant GW  as api-gateway<br/>:8080
    participant DES as ms-despacho<br/>:8088
    participant PED as ms-pedidos<br/>:8086

    Note over Operador: El operador programa el despacho

    Operador ->>+ GW  : POST /api/despachos<br/>{ pedidoId:1, repartidor:"Carlos", direccion:"Av. Providencia..." }
    GW       ->>+ DES : Enruta la petición
    Note over DES: Crea despacho con estado="EN_PREPARACION"<br/>fechaSalida = null
    DES      -->>- GW  : 201 Created → { id:1, estado:"EN_PREPARACION", ... }
    GW       -->> Operador : 201 Created ✅

    Note over Operador: El repartidor sale del local

    Operador ->>+ GW  : PATCH /api/despachos/1/estado?estado=EN_RUTA
    GW       ->>+ DES : Enruta la petición
    Note over DES: Actualiza estado="EN_RUTA"<br/>fechaSalida = NOW() ⏱️
    DES      -->>- GW  : 200 OK → { estado:"EN_RUTA", fechaSalida:"..." }
    GW       -->> Operador : 200 OK ✅

    Note over Operador: El repartidor confirma la entrega

    Operador ->>+ GW  : PUT /api/despachos/1<br/>{ ..., estado: "ENTREGADO" }
    GW       ->>+ DES : Enruta la petición

    Note over DES: Detecta estado="ENTREGADO"<br/>Registra fechaEntrega = NOW() ⏱️

    DES      ->>+ PED : [Feign] PUT /api/pedidos/1/estado?estado=ENTREGADO<br/>Cierra el ciclo del pedido
    PED      -->>- DES : 200 OK → Pedido cerrado como ENTREGADO ✅

    DES      -->>- GW  : 200 OK → { estado:"ENTREGADO", fechaEntrega:"..." }
    GW       -->> Operador : 200 OK ✅ Ciclo completo cerrado

    Note over PED: Estado final del pedido: ENTREGADO 🎉
```

---

## Flujo 4: Flujo Completo Integrado (Resumen)

```mermaid
sequenceDiagram
    autonumber
    actor       C  as Cliente
    actor       O  as Operador
    participant GW  as api-gateway
    participant PED as ms-pedidos
    participant CLI as ms-clientes
    participant NOT as ms-notificaciones
    participant COC as ms-cocina
    participant PAG as ms-pagos
    participant FAC as ms-facturacion
    participant DES as ms-despacho

    rect rgb(200, 230, 255)
        Note over C, NOT: FASE 1 — Pedido y Notificación
        C  ->>  GW  : POST /api/pedidos
        GW ->>  PED : crear pedido
        PED ->> CLI : [Feign] verifica cliente
        CLI -->> PED: OK
        PED ->> NOT : [Feign] notifica al cliente
        PED -->> GW : 201 → pedido CREADO
        GW -->>  C  : ✅ Pedido confirmado
    end

    rect rgb(220, 255, 220)
        Note over O, COC: FASE 2 — Preparación en Cocina
        O  ->>  GW  : POST /api/cocina/comandas
        GW ->>  COC : registrar comanda
        COC -->> GW : 201 → comanda PENDIENTE
        GW -->>  O  : ✅ Comanda registrada
        O  ->>  GW  : PATCH /api/cocina/comandas/1/estado?estado=EN_PREPARACION
        O  ->>  GW  : PATCH /api/cocina/comandas/1/estado?estado=LISTO
    end

    rect rgb(255, 235, 200)
        Note over C, FAC: FASE 3 — Pago y Facturación Automática
        C  ->>  GW  : POST /api/pagos
        GW ->>  PAG : registrar pago
        PAG ->> PED : [Feign] estado → PAGADO
        PAG ->> FAC : [Feign] emitir factura con IVA
        PAG -->> GW : 201 → pago APROBADO
        GW -->>  C  : ✅ Pago confirmado + Factura emitida
    end

    rect rgb(255, 220, 220)
        Note over O, DES: FASE 4 — Despacho y Cierre
        O  ->>  GW  : POST /api/despachos
        O  ->>  GW  : PATCH estado → EN_RUTA
        O  ->>  GW  : PUT despacho { estado: ENTREGADO }
        GW ->>  DES : actualizar despacho
        DES ->> PED : [Feign] estado → ENTREGADO
        DES -->> GW : 200 OK
        GW -->>  O  : ✅ Pedido cerrado como ENTREGADO 🎉
    end
```

---

## Resumen de Interacciones por Fase

| Fase | Acción | Servicios involucrados |
|------|--------|------------------------|
| **1. Pedido** | Cliente realiza orden | Gateway → ms-pedidos → ms-clientes (Feign) → ms-notificaciones (Feign) |
| **2. Cocina** | Chef prepara la comanda | Gateway → ms-cocina |
| **3. Pago** | Cliente paga la cuenta | Gateway → ms-pagos → ms-pedidos (Feign) → ms-facturacion (Feign) |
| **4. Despacho** | Repartidor entrega | Gateway → ms-despacho → ms-pedidos (Feign) |

---

## Escenarios de Error y Tolerancia a Fallos

| Escenario | Comportamiento del sistema |
|-----------|---------------------------|
| ms-clientes no disponible al crear pedido | ms-pedidos lanza excepción `RuntimeException`, pedido no se crea |
| ms-notificaciones no disponible | El pedido **sí se crea** — solo se registra un `log.error` (fallo silencioso) |
| ms-pedidos no disponible al pagar | El pago **sí se registra** en pagos_db — se loguea error de Feign |
| ms-facturacion no disponible al pagar | El pago **sí se registra** — la factura deberá emitirse manualmente |
| ms-pedidos no disponible al entregar | El despacho se marca `ENTREGADO` localmente — se loguea error de Feign |

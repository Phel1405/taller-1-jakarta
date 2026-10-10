# Arquitectura

## Diagrama de componentes

```mermaid
graph TB
    Cliente["Cliente (curl / requests.http)"]

    subgraph LT["logic-tier (WildFly, puerto 8081)"]
        Fachada["Recursos JAX-RS<br/>ProductController, PedidoController, InventarioController"]
        Servicios["Services (Product/Pedido)"]
        ClienteHTTP["DataTierClient<br/>(jakarta.ws.rs.client.Client)"]
        Fachada --> Servicios --> ClienteHTTP
    end

    subgraph DT["data-tier (WildFly, puerto 8082)"]
        Command["Command<br/>(entidades, @Transactional)"]
        Query["Query<br/>(proyecciones JPQL -> DTO)"]
        Saga["SagaOrquestador<br/>(sin @Transactional)"]
        TM["JTA TransactionManager<br/>(Narayana, 2PC)"]
        Command --> TM
        Saga --> Command
    end

    Postgres[("PostgreSQL<br/>pedidosPU: producto, inventario,<br/>pedido, detalle_pedido,<br/>reserva_inventario, saga_pedido")]
    MySQL[("MySQL<br/>pagosPU: pago")]

    Cliente -->|REST/JSON| Fachada
    ClienteHTTP -->|REST/JSON<br/>DATA_TIER_URL| Command
    ClienteHTTP -->|REST/JSON| Query
    TM -->|XA| Postgres
    TM -->|XA| MySQL
    Command -.->|JPA, pedidosPU| Postgres
    Query -.->|JPA, pedidosPU| Postgres
    Command -.->|JPA, pagosPU| MySQL
```

El logic-tier no tiene base de datos propia: es una fachada que valida
(Bean Validation) y traduce al data-tier. El data-tier separa comando y
consulta (CQRS) y coordina la saga; el `TransactionManager` de WildFly
(Narayana) es quien realmente ejecuta el 2PC cuando un mismo método toca
PostgreSQL y MySQL a la vez (paso "procesar pago").

## Diagrama de secuencia de la saga

```mermaid
sequenceDiagram
    actor Cliente
    participant LT as logic-tier
    participant Saga as SagaOrquestador
    participant Ped as PedidoCommandService
    participant Inv as InventarioCommandService
    participant Pago as PagoCommandService
    participant PG as PostgreSQL
    participant MY as MySQL

    Cliente->>LT: POST /pedidos
    LT->>Saga: ejecutar(request)

    rect rgb(230,245,230)
    Note over Saga,PG: Paso 1: CREAR (transacción local corta)
    Saga->>Ped: crearPendiente()
    Ped->>PG: INSERT pedido (PENDIENTE) + saga_pedido (EN_CURSO)
    PG-->>Ped: commit
    end

    rect rgb(230,245,230)
    Note over Saga,PG: Paso 2: RESERVAR (transacción local corta, con @Version)
    Saga->>Inv: reservar(items)
    Inv->>PG: UPDATE inventario (cantidadReservada) + INSERT reserva_inventario
    PG-->>Inv: commit (o OptimisticLockException -> reintento)
    end

    rect rgb(255,235,205)
    Note over Saga,MY: Paso 3: PAGAR (ÚNICA transacción XA / 2PC)
    Saga->>Pago: procesarPago(total)
    Pago->>MY: INSERT pago (APROBADO o RECHAZADO)
    Pago->>PG: UPDATE saga_pedido.pasoActual=PAGAR (+ pedido si aprobado)
    par Prepare
        Pago->>MY: XA PREPARE
        Pago->>PG: XA PREPARE
    end
    par Commit
        Pago->>MY: XA COMMIT
        Pago->>PG: XA COMMIT
    end
    end

    alt Pago aprobado
        rect rgb(230,245,230)
        Note over Saga,PG: Paso 4: CONFIRMAR (camino feliz)
        Saga->>Inv: confirmarReservas()
        Inv->>PG: reserva_inventario -> CONFIRMADA, descuenta cantidadDisponible
        Saga->>Ped: confirmar()
        Ped->>PG: pedido -> CONFIRMADO, saga -> COMPLETADA
        end
        Saga-->>LT: pedido CONFIRMADO
    else Pago rechazado
        rect rgb(250,220,220)
        Note over Saga,PG: Compensación
        Saga->>Inv: liberarReservas() (idempotente)
        Inv->>PG: reserva_inventario -> LIBERADA, revierte cantidadReservada
        Saga->>Ped: cancelar(motivo)
        Ped->>PG: pedido -> CANCELADO, saga -> COMPENSADA
        end
        Saga-->>LT: pedido CANCELADO
    end

    LT-->>Cliente: 201 Created (estado final)
```

Si el paso RESERVAR falla por stock insuficiente, no hay nada que
compensar (ninguna reserva llegó a confirmarse): el orquestador llama
directamente a `cancelar()` sobre el pedido. Si una compensación lanza una
excepción, el orquestador marca la saga como `FALLIDA` con el error, en
vez de dejarla en un estado ambiguo.

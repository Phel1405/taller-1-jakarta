# Taller No. 1 — Arquitectura de Software (Jakarta EE)

Aplicación distribuida en dos tiers Jakarta EE puros (sin Spring), desplegados
como WAR en WildFly 40.0.1.Final (JDK 21), comunicados por REST (JAX-RS), con
persistencia JPA/JTA sobre dos bases de datos (PostgreSQL y MySQL), una SAGA
por orquestación y CQRS en el lado de datos.

## Requisitos

- JDK 21
- Maven (o el wrapper `./mvnw` incluido en cada módulo)
- Docker y Docker Compose

## Compilar

```bash
mvn clean package
```

Genera `data-tier/target/jakartaee-data-tier.war` y
`logic-tier/target/jakartaee-logic-tier.war`.

## Levantar el stack

```bash
docker compose down -v      # limpia volúmenes si vienes de una versión anterior
docker compose up -d --build
```

Servicios:

| Servicio    | Puerto host | Descripción                         |
|-------------|-------------|--------------------------------------|
| logic-tier  | 8081        | Fachada pública (JAX-RS)             |
| data-tier   | 8082        | API interna de datos (JAX-RS + JPA)  |
| postgres    | 5433        | `pedidosPU`: producto, inventario, pedido, saga |
| mysql       | 3307        | `pagosPU`: pago                      |

La base pública es `http://localhost:8081/jakartaee-logic-tier/api`.

## Probar

```bash
bash scripts/demo.sh
```

Ejecuta: creación de productos, consulta de inventario, un pedido exitoso,
uno con pago rechazado y uno sin stock, y consulta el pedido y su saga.
También hay una colección `requests.http` con las mismas pruebas en formato
REST Client.

Verificación manual en las bases de datos:

```bash
docker compose exec postgres psql -U dev -d postgresdb -c "select id,estado,total from pedido; select * from saga_pedido;"
docker compose exec mysql mysql -udev -pTest1234 mysqldb -e "select * from pago;"
```

## Estructura de paquetes

```text
data-tier/com.example.data
├── entities/     Producto, Inventario, Pedido, DetallePedido,
│                 ReservaInventario, SagaPedido (pedidosPU) y Pago (pagosPU)
├── repository/   acceso a datos vía EntityManager (CDI, sin Spring Data)
├── command/      lado de ESCRITURA de CQRS: servicios @Transactional +
│                 recursos JAX-RS /api/commands/...
├── query/        lado de LECTURA de CQRS: proyecciones JPQL "SELECT NEW"
│                 a DTOs/records + recursos JAX-RS /api/queries/...
├── saga/         SagaOrquestador: CREAR -> RESERVAR -> PAGAR -> CONFIRMAR
├── dto/          records de entrada/salida de la API del data-tier
├── exception/    excepciones de dominio
└── web/          Application JAX-RS + ExceptionMapper -> {codigo, mensaje}

logic-tier/com.example.logic
├── controllers/  recursos JAX-RS públicos (/api/productos, /api/pedidos, /api/inventario)
├── services/     traducen DTOs públicos a llamadas al data-tier
├── client/       DataTierClient (jakarta.ws.rs.client.Client)
├── dtos/         records públicos (requests/responses)
├── exception/    DataTierErrorException, DataTierNoDisponibleException
└── web/          Application JAX-RS + ExceptionMapper -> {codigo, mensaje}
```

## Variables de entorno relevantes

- `DATA_TIER_URL` (logic-tier): URL base del data-tier.
  Por defecto `http://data-tier:8080/jakartaee-data-tier/api`.
- `PAGO_LIMITE` (data-tier): monto máximo que aprueba el pago simulado.
  Por defecto `1000000`.
- `POSTGRES_*`, `MYSQL_*` (`.env`): credenciales de ambas bases, usadas
  tanto por los contenedores de base de datos como por los datasources XA
  de WildFly (vía expresiones `${env.*}` resueltas en el arranque).

## Justificación de decisiones

### Orquestación vs. coreografía

Se eligió **orquestación** (`SagaOrquestador` centraliza CREAR → RESERVAR →
PAGAR → CONFIRMAR) porque el flujo es lineal y conocido de antemano, las
compensaciones son explícitas y fáciles de razonar en un único lugar, y el
estado de la saga queda centralizado en `saga_pedido`. La coreografía (cada
servicio reacciona a eventos del anterior) brilla con muchos participantes
desacoplados y un broker de mensajes, pero aquí habría añadido infraestructura
(colas, productores/consumidores) sin beneficio real para dos bases de datos
y cuatro pasos.

### JTA vs. SAGA

La saga completa **nunca** es una sola transacción JTA: cada paso
(`crearPendiente`, `reservar`, `confirmar`, `cancelar`, `liberarReservas`)
es una transacción local y corta (`@Transactional(REQUIRES_NEW)`) que
confirma o revierte por sí misma. Solo **procesar pago** abre una
transacción XA distribuida real: en el mismo método se escribe en MySQL
(`pago`) y en PostgreSQL (`pedido`, `saga_pedido`); al enlistar dos
XAResources en una misma transacción JTA, WildFly/Narayana coordina un
**two-phase commit (2PC)**. Si todo el flujo fuera una única transacción
JTA, perderíamos la posibilidad de comitear lo que ya es válido (ej.
el pedido creado) y compensar explícitamente lo demás; además una
transacción JTA larga que cruza llamadas HTTP y múltiples recursos durante
segundos/minutos es frágil y bloquea recursos innecesariamente.

### Nivel de CQRS elegido

CQRS "ligero": **misma base de datos**, pero **modelos y servicios
separados**. El lado de comando (`command/`) opera sobre entidades JPA
administradas y confirma transacciones. El lado de consulta (`query/`)
nunca devuelve entidades: usa proyecciones JPQL `SELECT NEW ...` directo a
records inmutables, con `TxType.SUPPORTS` (no abre transacciones de
escritura). No se usó una base de lectura separada ni reconstrucción por
eventos porque el volumen y la consistencia requerida no lo justifican en
este taller; aun así, la separación de paquetes y de modelos deja el camino
abierto para escalar a una base de lectura distinta si fuera necesario.

### REST y no gRPC entre tiers

Se reemplazó gRPC por JAX-RS/REST porque es el estilo de comunicación nativo
de Jakarta EE (sin generación de stubs ni dependencias de protobuf), más
simple de depurar con `curl`/`requests.http`, y suficiente para la
cardinalidad de llamadas entre dos tiers. El logic-tier usa
`jakarta.ws.rs.client.Client` apuntando a `DATA_TIER_URL` (configurable,
por defecto `http://data-tier:8080/jakartaee-data-tier/api`).

## Conceptos aplicados (resumen rápido)

- **XA / 2PC**: protocolo de confirmación en dos fases que permite que una
  transacción toque dos bases de datos distintas y, aun así, se confirme o
  revierta en ambas de forma atómica.
- **REQUIRES_NEW**: atributo de transacción que SIEMPRE abre una transacción
  nueva e independiente, incluso si ya hay una en curso — así cada paso de
  la saga es verdaderamente aislado y confirma por su cuenta.
- **Optimistic lock (`@Version`)**: en vez de bloquear filas, cada
  actualización verifica que nadie cambió el registro desde que lo leíste
  (comparando una columna de versión); si cambió, lanza
  `OptimisticLockException` y el llamador decide si reintenta.
- **Proyección JPQL (`SELECT NEW ...`)**: construye directamente un DTO/record
  desde la consulta, sin traer la entidad completa ni dejarla "managed" —
  ideal para el lado de lectura de CQRS.
- **Compensación**: acción inversa (liberar inventario, cancelar pedido) que
  deshace el efecto de un paso ya confirmado, cuando un paso posterior falla
  y ya no se puede hacer rollback de lo anterior.

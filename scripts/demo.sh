#!/usr/bin/env bash
# Demo end-to-end de la saga de pedidos a través del logic-tier.
# Requiere: docker compose up -d --build (y que WildFly ya haya desplegado).
set -euo pipefail

BASE="${BASE:-http://localhost:8081/jakartaee-logic-tier/api}"

linea() { printf '\n==== %s ====\n' "$1"; }
pedir() {
  local metodo="$1" path="$2" cuerpo="${3:-}"
  if [ -n "$cuerpo" ]; then
    curl -s -X "$metodo" "$BASE$path" -H "Content-Type: application/json" -d "$cuerpo" -w "\nHTTP %{http_code}\n"
  else
    curl -s -X "$metodo" "$BASE$path" -w "\nHTTP %{http_code}\n"
  fi
}

linea "1. Crear producto: Teclado (precio 150000, stock 10)"
RESP_TECLADO=$(curl -s -X POST "$BASE/productos" -H "Content-Type: application/json" \
  -d '{"nombre":"Teclado","descripcion":"Mecanico","precio":150000,"stockInicial":10}')
echo "$RESP_TECLADO"
ID_TECLADO=$(echo "$RESP_TECLADO" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

linea "1. Crear producto: Monitor (precio 900000, stock 5)"
RESP_MONITOR=$(curl -s -X POST "$BASE/productos" -H "Content-Type: application/json" \
  -d '{"nombre":"Monitor","descripcion":"4K","precio":900000,"stockInicial":5}')
echo "$RESP_MONITOR"
ID_MONITOR=$(echo "$RESP_MONITOR" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

linea "2. GET /inventario"
pedir GET /inventario

linea "3. Pedido EXITOSO: 2 teclados (total bajo el limite de pago)"
RESP_OK=$(curl -s -X POST "$BASE/pedidos" -H "Content-Type: application/json" \
  -d "{\"clienteId\":\"cliente-exitoso\",\"items\":[{\"productoId\":$ID_TECLADO,\"cantidad\":2}]}")
echo "$RESP_OK"
ID_PEDIDO_OK=$(echo "$RESP_OK" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

linea "4. Pedido con PAGO RECHAZADO: 2 monitores (total 1.800.000 > limite 1.000.000)"
RESP_RECHAZADO=$(curl -s -X POST "$BASE/pedidos" -H "Content-Type: application/json" \
  -d "{\"clienteId\":\"cliente-rechazado\",\"items\":[{\"productoId\":$ID_MONITOR,\"cantidad\":2}]}")
echo "$RESP_RECHAZADO"
ID_PEDIDO_RECHAZADO=$(echo "$RESP_RECHAZADO" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

linea "5. Pedido SIN STOCK: 50 monitores (solo hay 5-2=3 disponibles tras el pedido anterior)"
pedir POST /pedidos "{\"clienteId\":\"cliente-sin-stock\",\"items\":[{\"productoId\":$ID_MONITOR,\"cantidad\":50}]}"

linea "6. GET /inventario tras los 3 pedidos (el monitor debe quedar igual que al inicio: 5 disponibles, 0 reservados)"
pedir GET /inventario

linea "6. GET /pedidos/$ID_PEDIDO_OK (detalle del pedido exitoso)"
pedir GET "/pedidos/$ID_PEDIDO_OK"

linea "6. GET /pedidos/$ID_PEDIDO_OK/saga (saga COMPLETADA)"
pedir GET "/pedidos/$ID_PEDIDO_OK/saga"

linea "6. GET /pedidos/$ID_PEDIDO_RECHAZADO/saga (saga COMPENSADA)"
pedir GET "/pedidos/$ID_PEDIDO_RECHAZADO/saga"

linea "Fin de la demo"

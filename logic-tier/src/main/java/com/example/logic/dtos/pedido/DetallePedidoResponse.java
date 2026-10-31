package com.example.logic.dtos.pedido;

import java.math.BigDecimal;

public record DetallePedidoResponse(
    Long productoId,
    String nombreProducto,
    int cantidad,
    BigDecimal precioUnitario
) {
}

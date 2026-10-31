package com.example.data.dto;

import java.math.BigDecimal;

public record DetallePedidoDTO(
    Long productoId,
    String nombreProducto,
    int cantidad,
    BigDecimal precioUnitario
) {
}

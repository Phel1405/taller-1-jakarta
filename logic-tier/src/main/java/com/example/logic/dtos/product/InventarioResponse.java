package com.example.logic.dtos.product;

public record InventarioResponse(
    Long productoId,
    String nombreProducto,
    int cantidadDisponible,
    int cantidadReservada
) {
}

package com.example.data.dto;

public record InventarioDTO(
    Long productoId,
    String nombreProducto,
    int cantidadDisponible,
    int cantidadReservada
) {
}

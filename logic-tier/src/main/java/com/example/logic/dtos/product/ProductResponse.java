package com.example.logic.dtos.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    boolean disponible,
    int cantidadDisponible,
    int cantidadReservada,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion
) {
}

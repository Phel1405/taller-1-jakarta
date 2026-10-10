package com.example.data.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDTO(
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

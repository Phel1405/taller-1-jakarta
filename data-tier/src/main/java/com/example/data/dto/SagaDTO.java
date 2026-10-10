package com.example.data.dto;

import java.time.LocalDateTime;

public record SagaDTO(
    Long id,
    Long pedidoId,
    String pasoActual,
    String estado,
    String error,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin
) {
}

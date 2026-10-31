package com.example.logic.dtos.pedido;

import java.time.LocalDateTime;

public record SagaResponse(
    Long id,
    Long pedidoId,
    String pasoActual,
    String estado,
    String error,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin
) {
}

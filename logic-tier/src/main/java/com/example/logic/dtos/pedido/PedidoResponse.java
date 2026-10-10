package com.example.logic.dtos.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
    Long id,
    String clienteId,
    String estado,
    BigDecimal total,
    String motivoCancelacion,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<DetallePedidoResponse> detalles
) {
}

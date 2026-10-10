package com.example.data.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoDTO(
    Long id,
    String clienteId,
    String estado,
    BigDecimal total,
    String motivoCancelacion,
    LocalDateTime fechaCreacion,
    LocalDateTime fechaActualizacion,
    List<DetallePedidoDTO> detalles
) {
}

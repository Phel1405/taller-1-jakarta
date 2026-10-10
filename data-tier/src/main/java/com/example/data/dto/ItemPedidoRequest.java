package com.example.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(
    @NotNull Long productoId,
    @NotNull @Min(1) Integer cantidad
) {
}

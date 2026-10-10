package com.example.logic.dtos.pedido;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CrearPedidoRequest(
    @NotBlank String clienteId,
    @NotEmpty @Valid List<ItemPedidoRequest> items
) {
}

package com.example.logic.dtos.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ActualizarProductoRequest(
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal precio,
    @NotNull Boolean disponible
) {
}

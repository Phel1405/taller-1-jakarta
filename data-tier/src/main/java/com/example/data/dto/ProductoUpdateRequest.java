package com.example.data.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoUpdateRequest(
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal precio,
    @NotNull Boolean disponible
) {
}

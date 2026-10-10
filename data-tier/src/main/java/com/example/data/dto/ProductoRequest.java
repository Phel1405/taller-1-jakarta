package com.example.data.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ProductoRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal precio,
    @NotNull @Min(0) Integer stockInicial
) {
}

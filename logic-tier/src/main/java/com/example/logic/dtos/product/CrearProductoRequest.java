package com.example.logic.dtos.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CrearProductoRequest(
    @NotBlank String nombre,
    String descripcion,
    @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal precio,
    @NotNull @Min(0) Integer stockInicial
) {
}

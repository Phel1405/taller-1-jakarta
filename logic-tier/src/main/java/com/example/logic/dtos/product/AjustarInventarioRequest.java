package com.example.logic.dtos.product;

import jakarta.validation.constraints.NotNull;

public record AjustarInventarioRequest(
    @NotNull Integer delta
) {
}

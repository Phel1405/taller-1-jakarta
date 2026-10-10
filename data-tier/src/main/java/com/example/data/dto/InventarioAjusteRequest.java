package com.example.data.dto;

import jakarta.validation.constraints.NotNull;

public record InventarioAjusteRequest(
    @NotNull Integer delta
) {
}

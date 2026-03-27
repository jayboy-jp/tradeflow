package com.tradeflow.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateStockPriceRequest(
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        BigDecimal price
) {}


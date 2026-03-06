package com.tradeflow.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String userEmail,
        Long stockId,
        String stockSymbol,
        String type,
        BigDecimal price,
        Integer quantity,
        String status,
        Instant createdAt
) {}

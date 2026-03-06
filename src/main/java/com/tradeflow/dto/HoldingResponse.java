package com.tradeflow.dto;

import java.math.BigDecimal;

public record HoldingResponse(
        Long stockId,
        String symbol,
        String stockName,
        Integer quantity,
        BigDecimal currentPrice,
        BigDecimal currentValue
) {}

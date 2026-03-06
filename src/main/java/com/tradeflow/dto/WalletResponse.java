package com.tradeflow.dto;

import java.math.BigDecimal;

public record WalletResponse(
        BigDecimal availableBalance,
        BigDecimal lockedBalance,
        BigDecimal totalBalance
) {}

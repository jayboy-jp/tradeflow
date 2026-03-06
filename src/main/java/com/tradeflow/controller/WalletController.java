package com.tradeflow.controller;

import com.tradeflow.dto.WalletResponse;
import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallet")
@Tag(name = "Wallet", description = "Wallet balance and funds")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "Get wallet balance (available, locked, total)")
    public WalletResponse getWallet() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return walletService.getWallet(userId);
    }
}

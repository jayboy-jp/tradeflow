package com.tradeflow.controller;

import com.tradeflow.dto.HoldingResponse;
import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/portfolio")
@Tag(name = "Portfolio", description = "Holdings and positions")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    @Operation(summary = "Get current holdings")
    public List<HoldingResponse> getHoldings() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return portfolioService.getHoldings(userId);
    }
}

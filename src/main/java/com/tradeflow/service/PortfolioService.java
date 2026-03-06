package com.tradeflow.service;

import com.tradeflow.dto.HoldingResponse;
import com.tradeflow.entity.Portfolio;
import com.tradeflow.entity.User;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.PortfolioRepository;
import com.tradeflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    public PortfolioService(PortfolioRepository portfolioRepository, UserRepository userRepository) {
        this.portfolioRepository = portfolioRepository;
        this.userRepository = userRepository;
    }

    public List<HoldingResponse> getHoldings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        return portfolioRepository.findByUserOrderByStock_Symbol(user).stream()
                .map(this::toHoldingResponse)
                .collect(Collectors.toList());
    }

    private HoldingResponse toHoldingResponse(Portfolio p) {
        BigDecimal currentPrice = p.getStock().getCurrentPrice();
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(p.getQuantity()));
        return new HoldingResponse(
                p.getStock().getId(),
                p.getStock().getSymbol(),
                p.getStock().getName(),
                p.getQuantity(),
                currentPrice,
                currentValue
        );
    }
}

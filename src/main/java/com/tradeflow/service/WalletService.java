package com.tradeflow.service;

import com.tradeflow.dto.WalletResponse;
import com.tradeflow.entity.User;
import com.tradeflow.entity.Wallet;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.UserRepository;
import com.tradeflow.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    public WalletResponse getWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException("WALLET_NOT_FOUND", "Wallet not found"));
        return new WalletResponse(
                wallet.getAvailableBalance(),
                wallet.getLockedBalance(),
                wallet.getTotalBalance()
        );
    }
}

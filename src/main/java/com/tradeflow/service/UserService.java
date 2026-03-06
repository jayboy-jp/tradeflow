package com.tradeflow.service;

import com.tradeflow.entity.User;
import com.tradeflow.entity.Wallet;
import com.tradeflow.exception.BusinessException;
import com.tradeflow.repository.UserRepository;
import com.tradeflow.repository.WalletRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository,
                    PasswordEncoder passwordEncoder,
                    WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.walletRepository = walletRepository;
    }

    public User createUser(String email, String password) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException("USER_EXISTS", "User with this email already exists");
        }
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword);
        User savedUser = userRepository.save(user);
        Wallet wallet = new Wallet(savedUser);
        walletRepository.save(wallet);
        return savedUser;
    }

    public User getById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
    }

    public BigDecimal getWalletBalance(Long userId) {
        User user = getById(userId);
        return walletRepository.findByUser(user)
                .map(Wallet::getTotalBalance)
                .orElse(BigDecimal.ZERO);
    }

    public User addFunds(Long userId, BigDecimal amount) {
        User user = getById(userId);
        Wallet wallet = walletRepository.findByUser(user)
                .orElseGet(() -> walletRepository.save(new Wallet(user)));

        wallet.setTotalBalance(wallet.getTotalBalance().add(amount));
        walletRepository.save(wallet);

        return user;
    }
}

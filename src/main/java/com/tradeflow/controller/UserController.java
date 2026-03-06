package com.tradeflow.controller;

import com.tradeflow.dto.AddFundsRequest;
import com.tradeflow.dto.UserResponse;
import com.tradeflow.entity.User;
import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile and funds")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile with wallet balance")
    public UserResponse getProfile() {
        Long userId = SecurityUtils.requireCurrentUserId();
        User user = userService.getById(userId);
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                userService.getWalletBalance(userId)
        );
    }

    @PostMapping("/me/fund")
    @Operation(summary = "Add funds to wallet")
    public UserResponse addFunds(@Valid @RequestBody AddFundsRequest request) {
        Long userId = SecurityUtils.requireCurrentUserId();
        User user = userService.addFunds(userId, request.amount());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                userService.getWalletBalance(userId)
        );
    }
}

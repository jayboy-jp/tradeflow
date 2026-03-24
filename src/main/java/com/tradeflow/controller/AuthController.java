package com.tradeflow.controller;

import com.tradeflow.dto.AuthTokensResponse;
import com.tradeflow.dto.LoginRequest;
import com.tradeflow.dto.RefreshTokenRequest;
import com.tradeflow.dto.RegisterRequest;
import com.tradeflow.dto.UserResponse;
import com.tradeflow.entity.User;
import com.tradeflow.service.AuthService;
import com.tradeflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = userService.createUser(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get access + refresh tokens")
    public AuthTokensResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public AuthTokensResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshAccessToken(request.refreshToken());
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke a refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), userService.getWalletBalance(user.getId()));
    }
}

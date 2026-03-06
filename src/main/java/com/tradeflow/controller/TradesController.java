package com.tradeflow.controller;

import com.tradeflow.dto.OrderResponse;
import com.tradeflow.dto.PageResponse;
import com.tradeflow.entity.Order;
import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.OrderService;
import com.tradeflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trades", description = "Trade history (filled orders)")
public class TradesController {

    private final OrderService orderService;
    private final UserService userService;

    public TradesController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get trade history (filled orders)")
    public PageResponse<OrderResponse> getTrades(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.requireCurrentUserId();
        var user = userService.getById(userId);
        var orderPage = orderService.getFilledOrdersByUser(user, PageRequest.of(page, size));
        var content = orderPage.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isFirst(),
                orderPage.isLast()
        );
    }

    private OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getEmail(),
                order.getStock().getId(),
                order.getStock().getSymbol(),
                order.getType().name(),
                order.getPrice(),
                order.getQuantity(),
                order.getStatus().name(),
                order.getCreatedAt()
        );
    }
}

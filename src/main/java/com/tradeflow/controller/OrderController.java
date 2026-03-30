package com.tradeflow.controller;

import com.tradeflow.dto.OrderResponse;
import com.tradeflow.dto.PageResponse;
import com.tradeflow.dto.PlaceOrderRequest;
import com.tradeflow.dto.AsyncAcceptedResponse;
import com.tradeflow.entity.Order;
import com.tradeflow.entity.OrderStatus;
import com.tradeflow.messaging.OrderExecutionPublisher;
import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.OrderService;
import com.tradeflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@Tag(name = "Orders", description = "Place, list, execute and cancel orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final OrderExecutionPublisher orderExecutionPublisher;

    public OrderController(OrderService orderService,
                           UserService userService,
                           OrderExecutionPublisher orderExecutionPublisher) {
        this.orderService = orderService;
        this.userService = userService;
        this.orderExecutionPublisher = orderExecutionPublisher;
    }

    @PostMapping
    @Operation(summary = "Place a new order (BUY/SELL)")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Order order = orderService.placeOrder(
                userId,
                request.stockId(),
                request.type(),
                request.price(),
                request.quantity()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(order));
    }

    @GetMapping
    @Operation(summary = "List orders for current user (paginated)")
    public PageResponse<OrderResponse> listOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) OrderStatus status) {
        Long userId = SecurityUtils.requireCurrentUserId();
        var user = userService.getById(userId);
        var pageable = PageRequest.of(page, size);
        var orderPage = status != null
                ? orderService.getOrdersByUser(user, status, pageable)
                : orderService.getOrdersByUser(user, pageable);
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

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID")
    public OrderResponse getOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Order order = orderService.getOrderById(id, userId);
        return toResponse(order);
    }

    @PostMapping("/{id}/execute")
    @Operation(summary = "Execute a pending order")
    public OrderResponse executeOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Order order = orderService.getOrderById(id, userId);
        order = orderService.executeOrder(order.getId());
        return toResponse(order);
    }

    @PostMapping("/{id}/execute-async")
    @Operation(summary = "Queue a pending order for asynchronous execution")
    public ResponseEntity<AsyncAcceptedResponse> executeOrderAsync(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Order order = orderService.getOrderById(id, userId);
        orderExecutionPublisher.enqueueExecution(order.getId());
        return ResponseEntity.accepted().body(AsyncAcceptedResponse.accepted(order.getId()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending order")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        Long userId = SecurityUtils.requireCurrentUserId();
        Order order = orderService.getOrderById(id, userId);
        order = orderService.cancelOrder(order.getId());
        return toResponse(order);
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

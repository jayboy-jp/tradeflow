package com.tradeflow.controller;

import com.tradeflow.security.SecurityUtils;
import com.tradeflow.service.OrderRealtimeStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/stream")
@Tag(name = "Stream", description = "Real-time server-sent events")
public class StreamController {

    private final OrderRealtimeStreamService orderRealtimeStreamService;

    public StreamController(OrderRealtimeStreamService orderRealtimeStreamService) {
        this.orderRealtimeStreamService = orderRealtimeStreamService;
    }

    @GetMapping(value = "/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Subscribe to order updates (SSE)")
    public SseEmitter streamOrders() {
        Long userId = SecurityUtils.requireCurrentUserId();
        return orderRealtimeStreamService.subscribeOrders(userId);
    }
}


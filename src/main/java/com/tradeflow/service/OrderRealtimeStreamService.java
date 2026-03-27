package com.tradeflow.service;

import com.tradeflow.dto.OrderResponse;
import com.tradeflow.dto.OrderUpdateEvent;
import com.tradeflow.entity.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class OrderRealtimeStreamService {

    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter subscribeOrders(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());

        // Long-lived connection; client should reconnect if it drops.
        SseEmitter emitter = new SseEmitter(Duration.ofMinutes(30).toMillis());
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError((e) -> emitters.remove(emitter));

        return emitter;
    }

    public void publishOrderUpdate(Long userId, String eventType, Order order) {
        CopyOnWriteArrayList<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        OrderResponse orderResponse = toResponse(order);
        OrderUpdateEvent payload = new OrderUpdateEvent(eventType, orderResponse);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("order")
                                .data(payload, MediaType.APPLICATION_JSON)
                );
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        }
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


package com.tradeflow.dto;

public record OrderUpdateEvent(
        String event,
        OrderResponse order
) {}


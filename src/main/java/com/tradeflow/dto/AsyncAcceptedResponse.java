package com.tradeflow.dto;

public record AsyncAcceptedResponse(
        String status,
        String message,
        Long orderId
) {
    public static AsyncAcceptedResponse accepted(Long orderId) {
        return new AsyncAcceptedResponse(
                "ACCEPTED",
                "Order execution request queued for asynchronous processing",
                orderId
        );
    }
}


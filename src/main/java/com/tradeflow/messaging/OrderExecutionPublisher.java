package com.tradeflow.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderExecutionPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.order-exchange:tradeflow.order.exchange}")
    private String orderExchange;

    @Value("${app.rabbitmq.order-routing-key:tradeflow.order.execute}")
    private String orderRoutingKey;

    public OrderExecutionPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enqueueExecution(Long orderId) {
        rabbitTemplate.convertAndSend(orderExchange, orderRoutingKey, orderId);
    }
}


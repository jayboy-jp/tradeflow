package com.tradeflow.messaging;

import com.tradeflow.exception.BusinessException;
import com.tradeflow.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderExecutionConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderExecutionConsumer.class);

    private final OrderService orderService;

    @Value("${app.rabbitmq.order-queue:tradeflow.order.execution.queue}")
    private String orderQueue;

    public OrderExecutionConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @RabbitListener(queues = "${app.rabbitmq.order-queue:tradeflow.order.execution.queue}")
    public void consumeExecutionRequest(Long orderId) {
        try {
            orderService.executeOrder(orderId);
            log.info("Asynchronously executed orderId={} via queue={}", orderId, orderQueue);
        } catch (BusinessException ex) {
            // Business exceptions are expected for non-eligible orders; keep them pending.
            log.info("Skipped async execution for orderId={} reason={} message={}",
                    orderId, ex.getCode(), ex.getMessage());
        } catch (Exception ex) {
            log.error("Unexpected async execution failure for orderId={}", orderId, ex);
        }
    }
}


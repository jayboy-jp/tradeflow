package com.tradeflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${app.rabbitmq.order-exchange:tradeflow.order.exchange}")
    private String orderExchange;

    @Value("${app.rabbitmq.order-queue:tradeflow.order.execution.queue}")
    private String orderQueue;

    @Value("${app.rabbitmq.order-routing-key:tradeflow.order.execute}")
    private String orderRoutingKey;

    @Bean
    public Queue orderExecutionQueue() {
        return new Queue(orderQueue, true);
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange, true, false);
    }

    @Bean
    public Binding orderExecutionBinding(Queue orderExecutionQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderExecutionQueue).to(orderExchange).with(orderRoutingKey);
    }
}


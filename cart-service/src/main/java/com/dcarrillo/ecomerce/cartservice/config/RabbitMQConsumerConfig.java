package com.dcarrillo.ecomerce.cartservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {
    public static final String EXCHANGE_NAME = "product_exchange";
    public static final String ROUTING_KEY_PRODUCT_DELETED = "product.deleted.cart.event";
    public static final String QUEUE_DELETED_PRODUCT = "product.deleted.cart.queue";

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue productDeletedQueue() {
        return new Queue(QUEUE_DELETED_PRODUCT, true);
    }

    @Bean
    public Binding bindingProductDeleted(Queue productDeletedQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(productDeletedQueue)
                .to(productExchange)
                .with(ROUTING_KEY_PRODUCT_DELETED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}


package com.dcarrillo.ecomerce.orderservice.config;



import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE_NAME= "order_exchange";
    public static final String QUEUE_ORDERS_CREATED = "orders.created.events.queue";
    public static final String ROUTING_KEY_ORDER_CREATED = "order.created.event";

    @Bean
    public TopicExchange exchange(){
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue ordersCreatedQueque(){
        return new Queue(QUEUE_ORDERS_CREATED, true);
    }

    @Bean
    public Binding bindingOrderCreated(Queue orderCreatedQueue , TopicExchange orderExchange){
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(ROUTING_KEY_ORDER_CREATED);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}

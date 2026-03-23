package com.skillsync.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE            = "skillsync.exchange";
    public static final String SESSION_BOOKED_QUEUE = "session.booked.queue";
    public static final String SESSION_BOOKED_KEY  = "session.booked";

    // ─── Exchange ──────────────────────────────────────────────
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ─── Queues ────────────────────────────────────────────────
    @Bean
    public Queue sessionBookedQueue() {
        return new Queue(SESSION_BOOKED_QUEUE, true);
    }

    // ─── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding sessionBookedBinding() {
        return BindingBuilder
            .bind(sessionBookedQueue())
            .to(exchange())
            .with(SESSION_BOOKED_KEY);
    }

    // ─── JSON Converter ────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─── RabbitTemplate (for publishing) ───────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ─── Listener Container Factory (for consuming) ────────────
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
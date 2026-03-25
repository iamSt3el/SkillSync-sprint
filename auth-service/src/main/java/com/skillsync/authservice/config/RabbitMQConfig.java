package com.skillsync.authservice.config;

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

    public static final String EXCHANGE                   = "skillsync.exchange";
    public static final String USER_REGISTERED_QUEUE     = "user.registered.queue";
    public static final String USER_REGISTERED_KEY       = "user.registered";
    public static final String MENTOR_APPROVED_AUTH_QUEUE = "mentor.approved.auth.queue";
    public static final String MENTOR_APPROVED_KEY       = "mentor.approved";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue mentorApprovedAuthQueue() {
        return new Queue(MENTOR_APPROVED_AUTH_QUEUE, true);
    }

    @Bean
    public Binding mentorApprovedAuthBinding() {
        return BindingBuilder.bind(mentorApprovedAuthQueue()).to(exchange()).with(MENTOR_APPROVED_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jacksonMessageConverter());
        return template;
    }
}

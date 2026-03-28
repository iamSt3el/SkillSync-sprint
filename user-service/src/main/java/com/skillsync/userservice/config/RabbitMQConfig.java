package com.skillsync.userservice.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE                    = "skillsync.exchange";
    public static final String USER_REGISTERED_QUEUE      = "user.registered.queue";
    public static final String USER_REGISTERED_KEY        = "user.registered";
    public static final String MENTOR_APPROVED_USER_QUEUE = "mentor.approved.user.queue";
    public static final String MENTOR_APPROVED_KEY        = "mentor.approved";

    // ─── Exchange ──────────────────────────────────────────────
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    // ─── Queues ────────────────────────────────────────────────
    @Bean
    public Queue userBookedQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true);
    }

    @Bean
    public Queue mentorApprovedUserQueue() {
        return new Queue(MENTOR_APPROVED_USER_QUEUE, true);
    }

    // ─── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding sessionBookedBinding() {
        return BindingBuilder
            .bind(userBookedQueue())
            .to(topicExchange())
            .with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding mentorApprovedUserBinding() {
        return BindingBuilder
            .bind(mentorApprovedUserQueue())
            .to(topicExchange())
            .with(MENTOR_APPROVED_KEY);
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

}

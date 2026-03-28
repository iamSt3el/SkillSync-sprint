package com.skillsync.sessionservice.config;

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

    // All services share the single skillsync.exchange
    public static final String EXCHANGE = "skillsync.exchange";

    public static final String SESSION_BOOKED_KEY     = "session.booked";
    public static final String SESSION_ACCEPTED_KEY   = "session.accepted";
    public static final String SESSION_REJECTED_KEY   = "session.rejected";
    public static final String SESSION_CANCELLED_KEY  = "session.cancelled";
    public static final String SESSION_COMPLETED_KEY  = "session.completed";

    public static final String SESSION_BOOKED_QUEUE     = "session.booked.queue";
    public static final String SESSION_ACCEPTED_QUEUE   = "session.accepted.queue";
    public static final String SESSION_REJECTED_QUEUE   = "session.rejected.queue";
    public static final String SESSION_CANCELLED_QUEUE  = "session.cancelled.queue";
    public static final String SESSION_COMPLETED_QUEUE  = "session.completed.queue";

    public static final String PAYMENT_SUCCESS_KEY              = "payment.success";
    public static final String PAYMENT_FAILED_KEY               = "payment.failed";
    public static final String PAYMENT_SUCCESS_SESSION_QUEUE    = "session.payment.success.queue";
    public static final String PAYMENT_FAILED_SESSION_QUEUE     = "session.payment.failed.queue";

    // ─── Exchange ──────────────────────────────────────────────
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // ─── Queues ────────────────────────────────────────────────
    @Bean
    public Queue sessionBookedQueue() {
        return new Queue(SESSION_BOOKED_QUEUE, true);
    }

    @Bean
    public Queue sessionAcceptedQueue() {
        return new Queue(SESSION_ACCEPTED_QUEUE, true);
    }

    @Bean
    public Queue sessionRejectedQueue() {
        return new Queue(SESSION_REJECTED_QUEUE, true);
    }

    @Bean
    public Queue sessionCancelledQueue() {
        return new Queue(SESSION_CANCELLED_QUEUE, true);
    }

    @Bean
    public Queue sessionCompletedQueue() {
        return new Queue(SESSION_COMPLETED_QUEUE, true);
    }

    // ─── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding sessionBookedBinding() {
        return BindingBuilder.bind(sessionBookedQueue()).to(topicExchange()).with(SESSION_BOOKED_KEY);
    }

    @Bean
    public Binding sessionAcceptedBinding() {
        return BindingBuilder.bind(sessionAcceptedQueue()).to(topicExchange()).with(SESSION_ACCEPTED_KEY);
    }

    @Bean
    public Binding sessionRejectedBinding() {
        return BindingBuilder.bind(sessionRejectedQueue()).to(topicExchange()).with(SESSION_REJECTED_KEY);
    }

    @Bean
    public Binding sessionCancelledBinding() {
        return BindingBuilder.bind(sessionCancelledQueue()).to(topicExchange()).with(SESSION_CANCELLED_KEY);
    }

    @Bean
    public Binding sessionCompletedBinding() {
        return BindingBuilder.bind(sessionCompletedQueue()).to(topicExchange()).with(SESSION_COMPLETED_KEY);
    }

    @Bean public Queue paymentSuccessSessionQueue() { return new Queue(PAYMENT_SUCCESS_SESSION_QUEUE, true); }
    @Bean public Queue paymentFailedSessionQueue()  { return new Queue(PAYMENT_FAILED_SESSION_QUEUE,  true); }

    @Bean public Binding paymentSuccessSessionBinding() { return BindingBuilder.bind(paymentSuccessSessionQueue()).to(topicExchange()).with(PAYMENT_SUCCESS_KEY); }
    @Bean public Binding paymentFailedSessionBinding()  { return BindingBuilder.bind(paymentFailedSessionQueue()).to(topicExchange()).with(PAYMENT_FAILED_KEY); }

    // ─── JSON Converter ────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─── RabbitTemplate (for publishing) ───────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
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

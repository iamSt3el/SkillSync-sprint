package com.skillsync.notification_service.config;

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

    public static final String EXCHANGE                 = "skillsync.exchange";

    public static final String SESSION_BOOKED_QUEUE     = "session.booked.queue";
    public static final String SESSION_BOOKED_KEY       = "session.booked";
    public static final String SESSION_ACCEPTED_QUEUE   = "notification.session.accepted.queue";
    public static final String SESSION_ACCEPTED_KEY     = "session.accepted";
    public static final String SESSION_REJECTED_QUEUE   = "notification.session.rejected.queue";
    public static final String SESSION_REJECTED_KEY     = "session.rejected";
    public static final String SESSION_CANCELLED_QUEUE  = "notification.session.cancelled.queue";
    public static final String SESSION_CANCELLED_KEY    = "session.cancelled";
    public static final String SESSION_COMPLETED_QUEUE  = "notification.session.completed.queue";
    public static final String SESSION_COMPLETED_KEY    = "session.completed";

    public static final String USER_REGISTERED_QUEUE    = "notification.user.registered.queue";
    public static final String USER_REGISTERED_KEY      = "user.registered";

    public static final String GROUP_MEMBER_QUEUE       = "notification.group.queue";
    public static final String GROUP_MEMBER_JOINED_KEY  = "group.member.joined";
    public static final String GROUP_MEMBER_LEFT_KEY    = "group.member.left";

    public static final String MENTOR_APPROVED_QUEUE    = "notification.mentor.approved.queue";
    public static final String MENTOR_APPROVED_KEY      = "mentor.approved";

    public static final String REVIEW_SUBMITTED_QUEUE   = "notification.review.submitted.queue";
    public static final String REVIEW_SUBMITTED_KEY     = "review.submitted";

    public static final String PAYMENT_SUCCESS_QUEUE    = "notification.payment.success.queue";
    public static final String PAYMENT_SUCCESS_KEY      = "payment.success";
    public static final String PAYMENT_FAILED_QUEUE     = "notification.payment.failed.queue";
    public static final String PAYMENT_FAILED_KEY       = "payment.failed";


    // ─── Exchange ──────────────────────────────────────────────
    @Bean
    public TopicExchange topicExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).build();
    }

    // ─── Queues ────────────────────────────────────────────────
    @Bean
    public Queue sessionBookedQueue() {
        return new Queue(SESSION_BOOKED_QUEUE, true);
    }
    
    @Bean
    public Queue userRegisteredQueue() {
    	return new Queue(USER_REGISTERED_QUEUE, true);
    }
    
    @Bean
    public Queue groupNotificationQueue() {
    	return new Queue(GROUP_MEMBER_QUEUE, true);
    }

    @Bean public Queue sessionAcceptedNotifQueue()  { return new Queue(SESSION_ACCEPTED_QUEUE,  true); }
    @Bean public Queue sessionRejectedNotifQueue()  { return new Queue(SESSION_REJECTED_QUEUE,  true); }
    @Bean public Queue sessionCancelledNotifQueue() { return new Queue(SESSION_CANCELLED_QUEUE, true); }
    @Bean public Queue sessionCompletedNotifQueue() { return new Queue(SESSION_COMPLETED_QUEUE, true); }
    @Bean public Queue mentorApprovedNotifQueue()   { return new Queue(MENTOR_APPROVED_QUEUE,   true); }
    @Bean public Queue reviewSubmittedNotifQueue()  { return new Queue(REVIEW_SUBMITTED_QUEUE,  true); }
    @Bean public Queue paymentSuccessNotifQueue()   { return new Queue(PAYMENT_SUCCESS_QUEUE,   true); }
    @Bean public Queue paymentFailedNotifQueue()    { return new Queue(PAYMENT_FAILED_QUEUE,    true); }

    // ─── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding sessionBookedBinding() {
        return BindingBuilder
            .bind(sessionBookedQueue())
            .to(topicExchange())
            .with(SESSION_BOOKED_KEY);
    }

    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(userRegisteredQueue())
                .to(topicExchange())
                .with(USER_REGISTERED_KEY);
    }

    @Bean
    public Binding groupMemberJoinedBinding() {
        return BindingBuilder
                .bind(groupNotificationQueue())
                .to(topicExchange())
                .with(GROUP_MEMBER_JOINED_KEY);
    }
    
    @Bean
    public Binding groupMemberLeftBinding() {
    	return BindingBuilder.bind(groupNotificationQueue()).to(topicExchange()).with(GROUP_MEMBER_LEFT_KEY);
    }

    @Bean public Binding sessionAcceptedNotifBinding()  { return BindingBuilder.bind(sessionAcceptedNotifQueue()).to(topicExchange()).with(SESSION_ACCEPTED_KEY); }
    @Bean public Binding sessionRejectedNotifBinding()  { return BindingBuilder.bind(sessionRejectedNotifQueue()).to(topicExchange()).with(SESSION_REJECTED_KEY); }
    @Bean public Binding sessionCancelledNotifBinding() { return BindingBuilder.bind(sessionCancelledNotifQueue()).to(topicExchange()).with(SESSION_CANCELLED_KEY); }
    @Bean public Binding sessionCompletedNotifBinding() { return BindingBuilder.bind(sessionCompletedNotifQueue()).to(topicExchange()).with(SESSION_COMPLETED_KEY); }
    @Bean public Binding mentorApprovedNotifBinding()   { return BindingBuilder.bind(mentorApprovedNotifQueue()).to(topicExchange()).with(MENTOR_APPROVED_KEY); }
    @Bean public Binding reviewSubmittedNotifBinding()  { return BindingBuilder.bind(reviewSubmittedNotifQueue()).to(topicExchange()).with(REVIEW_SUBMITTED_KEY); }
    @Bean public Binding paymentSuccessNotifBinding()   { return BindingBuilder.bind(paymentSuccessNotifQueue()).to(topicExchange()).with(PAYMENT_SUCCESS_KEY); }
    @Bean public Binding paymentFailedNotifBinding()    { return BindingBuilder.bind(paymentFailedNotifQueue()).to(topicExchange()).with(PAYMENT_FAILED_KEY); }

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
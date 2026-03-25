package com.skillsync.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
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

    // ─── Bindings ──────────────────────────────────────────────
    @Bean
    public Binding sessionBookedBinding() {
        return BindingBuilder
            .bind(sessionBookedQueue())
            .to(exchange())
            .with(SESSION_BOOKED_KEY);
    }
    
    @Bean
    public Binding userRegisteredBinding() {
    	return BindingBuilder
    			.bind(userRegisteredQueue())
    			.to(exchange())
    			.with(USER_REGISTERED_KEY);
    }
    
    @Bean
    public Binding groupMemberJoinedBinding() {
    	return BindingBuilder
    			.bind(groupNotificationQueue())
    			.to(exchange())
    			.with(GROUP_MEMBER_JOINED_KEY);
    }
    
    @Bean
    public Binding groupMemberLeftBinding() {
    	return BindingBuilder.bind(groupNotificationQueue()).to(exchange()).with(GROUP_MEMBER_LEFT_KEY);
    }

    @Bean public Binding sessionAcceptedNotifBinding()  { return BindingBuilder.bind(sessionAcceptedNotifQueue()).to(exchange()).with(SESSION_ACCEPTED_KEY); }
    @Bean public Binding sessionRejectedNotifBinding()  { return BindingBuilder.bind(sessionRejectedNotifQueue()).to(exchange()).with(SESSION_REJECTED_KEY); }
    @Bean public Binding sessionCancelledNotifBinding() { return BindingBuilder.bind(sessionCancelledNotifQueue()).to(exchange()).with(SESSION_CANCELLED_KEY); }
    @Bean public Binding sessionCompletedNotifBinding() { return BindingBuilder.bind(sessionCompletedNotifQueue()).to(exchange()).with(SESSION_COMPLETED_KEY); }
    @Bean public Binding mentorApprovedNotifBinding()   { return BindingBuilder.bind(mentorApprovedNotifQueue()).to(exchange()).with(MENTOR_APPROVED_KEY); }
    @Bean public Binding reviewSubmittedNotifBinding()  { return BindingBuilder.bind(reviewSubmittedNotifQueue()).to(exchange()).with(REVIEW_SUBMITTED_KEY); }

    // ─── JSON Converter ────────────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
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
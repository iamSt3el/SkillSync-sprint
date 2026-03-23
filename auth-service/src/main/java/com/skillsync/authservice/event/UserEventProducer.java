package com.skillsync.authservice.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.skillsync.authservice.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventProducer {
	private final RabbitTemplate rabbitTemplate;
	
	public void publishUserRegistered(UserRegisteredEvent event) {
		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.USER_REGISTERED_KEY,
				event
				);
		log.info("Published UserRegisteredEvent for: {} " + event.getEmail());
	}
}

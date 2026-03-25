package com.skillsync.groupService.event;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.skillsync.groupService.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupNotificationProducer {
	private final RabbitTemplate rabbitTemplate;
	
	public void publishMemberJoined(UserNotificationEvent event) {
		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.GROUP_MEMBER_JOINED_KEY,
				event
				);
		log.info("Published UserNotificationEvent for joining a group: {}", event.getUserId());
	}
	
	public void publishMemberLeft(UserNotificationEvent event) {
		rabbitTemplate.convertAndSend(
				RabbitMQConfig.EXCHANGE,
				RabbitMQConfig.GROUP_MEMBER_LEFT_KEY,
				event
				);
		log.info("Published UserNotificationEvent for user left: {}", event.getUserId());
	}
}

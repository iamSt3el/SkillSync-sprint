package com.skillsync.userservice.event;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.repository.UserRepository;
import com.skillsync.userservice.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {
	private final UserService userService;
	private final UserRepository userRepository;
	
	@RabbitListener(queues = "user.registered.queue")
	public void handleUserRegistered(UserRegisteredEvent event) {
		if(userRepository.existsByEmail(event.getEmail())) {
			log.warn("User already exists, skipping: {}", event.getEmail());
			return;
		}
		User user = new User(
				event.getUserName(),
				event.getUserName(),
				event.getEmail(),
				event.getPassword(),
				event.getRole()
				);
		user.setId(event.getUserId());
		userService.createUser(user);
		log.info("Created User profile for: {}", event.getEmail());
	}
}

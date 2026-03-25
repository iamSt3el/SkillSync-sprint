package com.skillsync.userservice.event;

import com.skillsync.userservice.config.RabbitMQConfig;
import com.skillsync.userservice.entity.User;
import com.skillsync.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MentorApprovedConsumer {

    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.MENTOR_APPROVED_USER_QUEUE)
    @Transactional
    public void handleMentorApproved(MentorApprovedEvent event) {
        Long userId = event.getUserId();
        log.info("Updating role to ROLE_MENTOR for userId={}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found with id={}, skipping role update", userId);
            return;
        }

        user.setRole("ROLE_MENTOR");
        userRepository.save(user);
        log.info("Role updated to ROLE_MENTOR for userId={}", userId);
    }
}

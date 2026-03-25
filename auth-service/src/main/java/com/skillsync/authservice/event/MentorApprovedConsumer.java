package com.skillsync.authservice.event;

import com.skillsync.authservice.config.RabbitMQConfig;
import com.skillsync.authservice.entity.Role;
import com.skillsync.authservice.entity.User;
import com.skillsync.authservice.entity.UserRole;
import com.skillsync.authservice.repository.RoleRepository;
import com.skillsync.authservice.repository.UserRepository;
import com.skillsync.authservice.repository.UserRoleRepository;
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
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @RabbitListener(queues = RabbitMQConfig.MENTOR_APPROVED_AUTH_QUEUE)
    @Transactional
    public void handleMentorApproved(MentorApprovedEvent event) {
        Long userId = event.getUserId();
        log.info("Updating role to ROLE_MENTOR for userId={}", userId);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("User not found with id={}, skipping role update", userId);
            return;
        }

        userRoleRepository.deleteByIdUserId(userId);

        Role mentorRole = roleRepository.findByName("ROLE_MENTOR")
                .orElseThrow(() -> new RuntimeException("ROLE_MENTOR not found in DB"));

        userRoleRepository.save(new UserRole(user, mentorRole));
        log.info("Role updated to ROLE_MENTOR for userId={}", userId);
    }
}

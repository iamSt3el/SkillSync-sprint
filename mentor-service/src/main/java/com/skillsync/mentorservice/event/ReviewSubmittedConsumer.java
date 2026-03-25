package com.skillsync.mentorservice.event;

import com.skillsync.mentorservice.config.RabbitMQConfig;
import com.skillsync.mentorservice.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSubmittedConsumer {

    private final MentorService mentorService;

    @RabbitListener(queues = RabbitMQConfig.REVIEW_SUBMITTED_MENTOR_QUEUE)
    public void handleReviewSubmitted(ReviewSubmittedEvent event) {
        log.info("Updating rating for mentorId={} with new rating={}", event.getMentorId(), event.getRating());
        mentorService.updateRating(event.getMentorId(), event.getRating());
    }
}

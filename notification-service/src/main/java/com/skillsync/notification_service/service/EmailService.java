package com.skillsync.notification_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.skillsync.notification_service.exception.EmailSendException;

@Service
public class EmailService {

    private final Resend resend;

    public EmailService(@Value("${resend.api-key}") String apiKey) {
        this.resend = new Resend(apiKey); 
    }

    public void sendEmail(String toEmail, String subject, String body) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                .from("onboarding@resend.dev")
                .to(toEmail)
                .subject(subject)
                .text(body)
                .build();

            resend.emails().send(params);

        } catch (ResendException e) {
            throw new EmailSendException("Failed to send email to " + toEmail, e);
        }
    }
}

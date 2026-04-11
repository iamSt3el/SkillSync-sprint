package com.skillsync.notification_service.service;

public interface EmailService {

    void sendEmail(String toEmail, String subject, String body);
}

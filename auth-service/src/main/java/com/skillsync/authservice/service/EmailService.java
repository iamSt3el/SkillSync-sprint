package com.skillsync.authservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        // Use placeholder replacement instead of .formatted() to avoid
        // Java treating CSS "%" characters (e.g. "0%", "100%") as format specifiers.
        String html = """
            <div style="font-family: Roboto, Arial, sans-serif; max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;">
              <div style="background: linear-gradient(135deg, #0d1b2a 0%, #1a2d42 100%); padding: 32px 40px; text-align: center;">
                <h1 style="margin: 0; color: #ffffff; font-size: 1.5rem; letter-spacing: -0.5px;">
                  <span style="color: #ffffff;">Skill</span><span style="color: #f59e0b;">Sync</span>
                </h1>
              </div>
              <div style="padding: 40px;">
                <h2 style="color: #0f172a; font-size: 1.25rem; margin: 0 0 12px;">Reset your password</h2>
                <p style="color: #475569; line-height: 1.6; margin: 0 0 28px;">
                  We received a request to reset the password for your SkillSync account.
                  Click the button below to choose a new password. This link is valid for <strong>15 minutes</strong>.
                </p>
                <a href="{{RESET_LINK}}"
                   style="display: inline-block; background: #DD0031; color: #ffffff; text-decoration: none;
                          padding: 14px 32px; border-radius: 10px; font-weight: 700; font-size: 1rem;">
                  Reset Password
                </a>
                <p style="color: #94a3b8; font-size: 0.85rem; margin: 28px 0 0; line-height: 1.5;">
                  If you didn't request this, you can safely ignore this email — your password won't change.<br>
                  Or copy this link: <span style="color: #DD0031;">{{RESET_LINK}}</span>
                </p>
              </div>
              <div style="background: #f8fafc; padding: 20px 40px; text-align: center; border-top: 1px solid #f1f5f9;">
                <p style="color: #cbd5e1; font-size: 0.8rem; margin: 0;">© 2026 SkillSync. Empowering careers worldwide.</p>
              </div>
            </div>
            """.replace("{{RESET_LINK}}", resetLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reset your SkillSync password");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send reset email. Please try again later.");
        }
    }
}

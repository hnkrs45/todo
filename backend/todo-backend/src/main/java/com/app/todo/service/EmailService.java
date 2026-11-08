package com.app.todo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            // Replace with your frontend host or verification endpoint
            String verificationUrl = "http://localhost:8080/api/v1/auth/verify?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Verify your ToDo App Account");
            message.setText("Welcome! Please click the link below to verify your account:\n\n"
                    + verificationUrl
                    + "\n\nThis link will expire in 24 hours.");

            mailSender.send(message);
            log.info("Verification email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            // Token is still stored in DB so verification can be attempted or resent
        }
    }
}
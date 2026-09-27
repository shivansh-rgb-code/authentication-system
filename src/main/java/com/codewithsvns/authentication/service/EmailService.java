package com.codewithsvns.authentication.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Your Verification OTP");
        message.setText(
                "Your OTP for account verification is: " + otp +
                        "\n\nThis OTP will expire in 5 minutes."
        );

        mailSender.send(message);
    }
}
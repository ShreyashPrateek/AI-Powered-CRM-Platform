package com.crm.notification.service;

import com.crm.notification.entity.Notification;
import com.crm.notification.enums.NotificationStatus;
import com.crm.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final NotificationRepository repository;

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void send(Notification notification) {
        // recipient email is stored in referenceUrl field when type=EMAIL
        // For a real system, look up user email from user-service; here we use referenceUrl as recipient
        String to = notification.getReferenceUrl();
        if (to == null || !to.contains("@")) {
            log.warn("No valid email recipient for notification id={}", notification.getId());
            notification.setStatus(NotificationStatus.FAILED);
            repository.save(notification);
            return;
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromAddress);
        mail.setTo(to);
        mail.setSubject(notification.getTitle());
        mail.setText(notification.getMessage());
        mailSender.send(mail);

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        repository.save(notification);
        log.info("Email notification sent to {} for event={}", to, notification.getSourceEvent());
    }
}

package com.crm.notification.service;

import com.crm.notification.entity.Notification;
import com.crm.notification.enums.NotificationStatus;
import com.crm.notification.repository.NotificationRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class SmsService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.from-number}")
    private String fromNumber;

    private final NotificationRepository repository;

    public SmsService(NotificationRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void init() {
        Twilio.init(accountSid, authToken);
    }

    public void send(Notification notification) {
        // referenceUrl holds the recipient phone number when type=SMS
        String to = notification.getReferenceUrl();
        if (to == null || !to.startsWith("+")) {
            log.warn("No valid phone number for notification id={}", notification.getId());
            notification.setStatus(NotificationStatus.FAILED);
            repository.save(notification);
            return;
        }

        Message.creator(new PhoneNumber(to), new PhoneNumber(fromNumber),
                notification.getTitle() + "\n" + notification.getMessage())
            .create();

        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(Instant.now());
        repository.save(notification);
        log.info("SMS notification sent to {} for event={}", to, notification.getSourceEvent());
    }
}

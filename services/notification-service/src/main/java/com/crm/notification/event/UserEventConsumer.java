package com.crm.notification.event;

import com.crm.notification.enums.NotificationType;
import com.crm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-events", groupId = "notification-service",
                   containerFactory = "userKafkaListenerContainerFactory")
    public void onUserEvent(UserEventMessage event) {
        log.debug("Received UserEvent [{}] userId={}", event.eventType(), event.userId());

        if (event.userId() == null) return;

        String title   = buildTitle(event);
        String message = buildMessage(event);

        notificationService.dispatch(event.userId(), NotificationType.IN_APP,
            title, message, event.eventType(), "/users/" + event.userId());

        notificationService.dispatch(event.userId(), NotificationType.WEBSOCKET,
            title, message, event.eventType(), "/users/" + event.userId());

        // Welcome email on account creation
        if ("USER_CREATED".equals(event.eventType()) && event.email() != null) {
            notificationService.dispatch(event.userId(), NotificationType.EMAIL,
                "Welcome to CRM Platform",
                "Hi, your account has been created successfully. Role: " + event.crmRole(),
                event.eventType(), event.email());
        }
    }

    private String buildTitle(UserEventMessage e) {
        return switch (e.eventType()) {
            case "USER_CREATED"     -> "Account Created";
            case "USER_UPDATED"     -> "Profile Updated";
            case "USER_DEACTIVATED" -> "Account Deactivated";
            default                 -> "Account Notification";
        };
    }

    private String buildMessage(UserEventMessage e) {
        return switch (e.eventType()) {
            case "USER_CREATED"     -> "Your CRM account has been created with role: " + e.crmRole() + ".";
            case "USER_UPDATED"     -> "Your profile has been updated.";
            case "USER_DEACTIVATED" -> "Your account has been deactivated. Contact support if this is unexpected.";
            default                 -> "An update occurred on your account.";
        };
    }
}

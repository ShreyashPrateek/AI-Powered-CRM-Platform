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
public class LeadEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "lead-events", groupId = "notification-service",
                   containerFactory = "kafkaListenerContainerFactory")
    public void onLeadEvent(LeadEventMessage event) {
        log.debug("Received LeadEvent [{}] leadId={}", event.eventType(), event.leadId());

        if (event.assignedUserId() == null) return;

        String title   = buildTitle(event);
        String message = buildMessage(event);

        // In-app + WebSocket alert for the assigned user
        notificationService.dispatch(event.assignedUserId(), NotificationType.IN_APP,
            title, message, event.eventType(), "/leads/" + event.leadId());

        notificationService.dispatch(event.assignedUserId(), NotificationType.WEBSOCKET,
            title, message, event.eventType(), "/leads/" + event.leadId());

        // Email notification — referenceUrl carries the recipient address
        if (event.leadEmail() != null) {
            notificationService.dispatch(event.assignedUserId(), NotificationType.EMAIL,
                title, message, event.eventType(), event.leadEmail());
        }
    }

    private String buildTitle(LeadEventMessage e) {
        return switch (e.eventType()) {
            case "LEAD_CREATED"        -> "New Lead Assigned";
            case "LEAD_ASSIGNED"       -> "Lead Assigned to You";
            case "LEAD_STATUS_CHANGED" -> "Lead Status Updated";
            case "LEAD_UPDATED"        -> "Lead Updated";
            case "LEAD_DELETED"        -> "Lead Removed";
            default                    -> "Lead Notification";
        };
    }

    private String buildMessage(LeadEventMessage e) {
        return switch (e.eventType()) {
            case "LEAD_CREATED"        -> "A new lead (ID: " + e.leadId() + ") has been created and assigned to you.";
            case "LEAD_ASSIGNED"       -> "Lead ID " + e.leadId() + " has been assigned to you.";
            case "LEAD_STATUS_CHANGED" -> "Lead ID " + e.leadId() + " status changed to " + e.status() + ".";
            case "LEAD_UPDATED"        -> "Lead ID " + e.leadId() + " has been updated.";
            case "LEAD_DELETED"        -> "Lead ID " + e.leadId() + " has been removed from the system.";
            default                    -> "An update occurred on lead ID " + e.leadId() + ".";
        };
    }
}

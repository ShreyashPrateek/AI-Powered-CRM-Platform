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
public class DealEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "deal-events", groupId = "notification-service",
                   containerFactory = "dealKafkaListenerContainerFactory")
    public void onDealEvent(DealEventMessage event) {
        log.debug("Received DealEvent [{}] dealId={}", event.eventType(), event.dealId());

        if (event.ownerId() == null) return;

        String title   = buildTitle(event);
        String message = buildMessage(event);

        notificationService.dispatch(event.ownerId(), NotificationType.IN_APP,
            title, message, event.eventType(), "/deals/" + event.dealId());

        notificationService.dispatch(event.ownerId(), NotificationType.WEBSOCKET,
            title, message, event.eventType(), "/deals/" + event.dealId());
    }

    private String buildTitle(DealEventMessage e) {
        return switch (e.eventType()) {
            case "DEAL_CREATED"       -> "New Deal Created";
            case "DEAL_STAGE_CHANGED" -> "Deal Stage Changed";
            case "DEAL_ASSIGNED"      -> "Deal Assigned to You";
            case "DEAL_UPDATED"       -> "Deal Updated";
            case "DEAL_DELETED"       -> "Deal Removed";
            default                   -> "Deal Notification";
        };
    }

    private String buildMessage(DealEventMessage e) {
        return switch (e.eventType()) {
            case "DEAL_CREATED"       -> "Deal ID " + e.dealId() + " worth $" + e.value() + " has been created.";
            case "DEAL_STAGE_CHANGED" -> "Deal ID " + e.dealId() + " moved to stage: " + e.stage() + ".";
            case "DEAL_ASSIGNED"      -> "Deal ID " + e.dealId() + " has been assigned to you.";
            case "DEAL_UPDATED"       -> "Deal ID " + e.dealId() + " has been updated.";
            case "DEAL_DELETED"       -> "Deal ID " + e.dealId() + " has been removed.";
            default                   -> "An update occurred on deal ID " + e.dealId() + ".";
        };
    }
}

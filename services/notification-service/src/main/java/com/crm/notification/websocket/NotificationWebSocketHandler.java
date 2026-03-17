package com.crm.notification.websocket;

import com.crm.notification.dto.NotificationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pushes a notification to the user-specific queue: /user/{userId}/queue/notifications
     */
    public void sendToUser(Long userId, NotificationDto.Response notification) {
        messagingTemplate.convertAndSendToUser(
            String.valueOf(userId),
            "/queue/notifications",
            notification
        );
        log.debug("WebSocket alert pushed to userId={} event={}", userId, notification.sourceEvent());
    }

    /**
     * Broadcasts a notification to all subscribers of a topic, e.g. system-wide alerts.
     */
    public void broadcast(String topic, NotificationDto.Response notification) {
        messagingTemplate.convertAndSend("/topic/" + topic, notification);
        log.debug("WebSocket broadcast on topic={}", topic);
    }
}

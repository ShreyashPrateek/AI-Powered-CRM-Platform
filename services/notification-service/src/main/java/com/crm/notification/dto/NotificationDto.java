package com.crm.notification.dto;

import com.crm.notification.enums.NotificationStatus;
import com.crm.notification.enums.NotificationType;

import java.time.Instant;

public class NotificationDto {

    public record Response(
        Long id,
        Long userId,
        NotificationType type,
        NotificationStatus status,
        String title,
        String message,
        String sourceEvent,
        String referenceUrl,
        Instant createdAt,
        Instant sentAt,
        Instant readAt
    ) {}

    public record UnreadCountResponse(long count) {}
}

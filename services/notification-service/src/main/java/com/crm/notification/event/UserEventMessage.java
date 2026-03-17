package com.crm.notification.event;

import java.time.Instant;

public record UserEventMessage(
    String eventType,
    Long userId,
    Long authUserId,
    String email,
    String crmRole,
    Long teamId,
    Instant occurredAt
) {}

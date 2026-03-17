package com.crm.notification.event;

import java.time.Instant;

public record LeadEventMessage(
    String eventType,
    Long leadId,
    String leadEmail,
    String status,
    Long assignedUserId,
    Instant occurredAt
) {}

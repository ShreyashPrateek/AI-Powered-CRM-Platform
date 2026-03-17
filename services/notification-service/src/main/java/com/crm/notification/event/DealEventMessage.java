package com.crm.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

public record DealEventMessage(
    String eventType,
    Long dealId,
    Long leadId,
    String stage,
    BigDecimal value,
    Long ownerId,
    Instant occurredAt
) {}

package com.crm.analytics.event;

import java.math.BigDecimal;
import java.time.Instant;

public record DealEventMessage(
    String     eventType,   // DEAL_CREATED, DEAL_UPDATED, DEAL_STAGE_CHANGED, DEAL_ASSIGNED, DEAL_DELETED
    Long       dealId,
    Long       leadId,
    String     stage,
    BigDecimal value,
    Long       ownerId,
    Instant    occurredAt
) {}

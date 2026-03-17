package com.crm.deal.event;

import com.crm.deal.enums.DealStage;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
public record DealEvent(
    String eventType,       // DEAL_CREATED, DEAL_UPDATED, DEAL_STAGE_CHANGED, DEAL_ASSIGNED, DEAL_DELETED
    Long dealId,
    Long leadId,
    DealStage stage,
    BigDecimal value,
    Long ownerId,
    Instant occurredAt
) {}

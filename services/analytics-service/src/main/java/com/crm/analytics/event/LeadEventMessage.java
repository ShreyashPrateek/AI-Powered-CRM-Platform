package com.crm.analytics.event;

import java.time.Instant;

public record LeadEventMessage(
    String  eventType,      // LEAD_CREATED, LEAD_UPDATED, LEAD_ASSIGNED, LEAD_STATUS_CHANGED, LEAD_DELETED
    Long    leadId,
    String  leadEmail,
    String  status,
    Long    assignedUserId,
    Instant occurredAt
) {}

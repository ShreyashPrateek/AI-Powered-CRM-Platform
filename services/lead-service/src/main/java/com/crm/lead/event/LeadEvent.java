package com.crm.lead.event;

import com.crm.lead.enums.LeadStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record LeadEvent(
    String eventType,       // LEAD_CREATED, LEAD_UPDATED, LEAD_ASSIGNED, LEAD_STATUS_CHANGED, LEAD_DELETED
    Long leadId,
    String leadEmail,
    LeadStatus status,
    Long assignedUserId,
    Instant occurredAt
) {}

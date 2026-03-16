package com.crm.user.event;

import com.crm.user.enums.CrmRole;
import lombok.Builder;

import java.time.Instant;

@Builder
public record UserEvent(
    String eventType,       // USER_CREATED, USER_UPDATED, USER_DEACTIVATED
    Long userId,
    Long authUserId,
    String email,
    CrmRole crmRole,
    Long teamId,
    Instant occurredAt
) {}

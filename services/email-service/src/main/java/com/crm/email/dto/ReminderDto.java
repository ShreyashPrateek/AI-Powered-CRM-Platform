package com.crm.email.dto;

import com.crm.email.enums.ReminderStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;

public class ReminderDto {

    public record CreateRequest(
        @NotNull Long leadId,
        @NotBlank @Email String leadEmail,
        String leadName,
        Long assignedUserId,
        @NotBlank String subject,
        @NotBlank String body,
        @NotNull Instant scheduledAt
    ) {}

    public record UpdateRequest(
        String subject,
        String body,
        Instant scheduledAt
    ) {}

    @Builder
    public record Response(
        Long id,
        Long leadId,
        String leadEmail,
        String leadName,
        Long assignedUserId,
        String subject,
        String body,
        Instant scheduledAt,
        ReminderStatus status,
        Instant sentAt,
        Instant createdAt
    ) {}
}

package com.crm.email.dto;

import com.crm.email.enums.CampaignStatus;
import com.crm.email.enums.EmailStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public class CampaignDto {

    public record RecipientRequest(
        @NotBlank @Email String email,
        String name,
        Long leadId
    ) {}

    public record CreateRequest(
        @NotBlank String name,
        @NotNull Long templateId,
        @NotNull Long createdBy,
        Instant scheduledAt,
        @NotEmpty List<RecipientRequest> recipients
    ) {}

    public record UpdateRequest(
        String name,
        Long templateId,
        Instant scheduledAt
    ) {}

    @Builder
    public record RecipientResponse(
        Long id,
        String recipientEmail,
        String recipientName,
        Long leadId,
        EmailStatus status,
        String failureReason,
        Instant sentAt
    ) {}

    @Builder
    public record Response(
        Long id,
        String name,
        Long templateId,
        String templateName,
        CampaignStatus status,
        Instant scheduledAt,
        Instant sentAt,
        Long createdBy,
        int totalRecipients,
        long sentCount,
        long failedCount,
        Instant createdAt,
        Instant updatedAt
    ) {}
}

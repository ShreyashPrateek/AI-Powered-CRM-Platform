package com.crm.email.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Instant;

public class EmailTemplateDto {

    public record CreateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank @Size(max = 100) String templateKey,
        @Size(max = 500) String description
    ) {}

    public record UpdateRequest(
        @Size(max = 255) String subject,
        @Size(max = 500) String description,
        Boolean active
    ) {}

    @Builder
    public record Response(
        Long id,
        String name,
        String subject,
        String templateKey,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
    ) {}
}

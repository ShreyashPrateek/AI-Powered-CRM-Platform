package com.crm.user.dto;

import com.crm.user.enums.CrmRole;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;

public class UserProfileDto {

    public record CreateRequest(
        @NotNull Long authUserId,
        @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 100) String email,
        @Size(max = 20) String phone,
        @Size(max = 100) String jobTitle,
        @Size(max = 255) String avatarUrl,
        @NotNull CrmRole crmRole,
        Long teamId
    ) {}

    public record UpdateRequest(
        @Size(max = 100) String fullName,
        @Size(max = 20) String phone,
        @Size(max = 100) String jobTitle,
        @Size(max = 255) String avatarUrl,
        CrmRole crmRole,
        Long teamId
    ) {}

    @Builder
    public record Response(
        Long id,
        Long authUserId,
        String fullName,
        String email,
        String phone,
        String jobTitle,
        String avatarUrl,
        CrmRole crmRole,
        Long teamId,
        String teamName,
        boolean active,
        Instant createdAt,
        Instant updatedAt
    ) {}
}

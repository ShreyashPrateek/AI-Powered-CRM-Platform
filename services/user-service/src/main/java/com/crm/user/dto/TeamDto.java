package com.crm.user.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;

public class TeamDto {

    public record CreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description,
        @NotNull Long departmentId,
        @NotNull Long managerAuthId
    ) {}

    public record UpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String description,
        Long departmentId,
        Long managerAuthId
    ) {}

    @Builder
    public record Response(
        Long id,
        String name,
        String description,
        Long departmentId,
        String departmentName,
        Long managerAuthId,
        int memberCount,
        Instant createdAt,
        Instant updatedAt
    ) {}
}

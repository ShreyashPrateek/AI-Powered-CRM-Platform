package com.crm.user.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;

public class DepartmentDto {

    public record CreateRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
    ) {}

    public record UpdateRequest(
        @Size(max = 100) String name,
        @Size(max = 255) String description
    ) {}

    @Builder
    public record Response(
        Long id,
        String name,
        String description,
        int teamCount,
        Instant createdAt,
        Instant updatedAt
    ) {}
}

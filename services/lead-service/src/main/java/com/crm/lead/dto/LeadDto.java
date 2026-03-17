package com.crm.lead.dto;

import com.crm.lead.enums.Industry;
import com.crm.lead.enums.LeadSource;
import com.crm.lead.enums.LeadStatus;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.time.Instant;
import java.util.List;

public class LeadDto {

    public record CreateRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(max = 20) String phone,
        @Size(max = 150) String company,
        Industry industry,
        @NotNull LeadSource source,
        Long assignedUserId,
        String notes
    ) {}

    public record UpdateRequest(
        @Size(max = 150) String name,
        @Size(max = 20) String phone,
        @Size(max = 150) String company,
        Industry industry,
        LeadSource source,
        String notes
    ) {}

    public record AssignRequest(
        @NotNull Long assignedUserId
    ) {}

    public record StatusUpdateRequest(
        @NotNull LeadStatus status
    ) {}

    @Builder
    public record Response(
        Long id,
        String name,
        String email,
        String phone,
        String company,
        Industry industry,
        LeadSource source,
        LeadStatus status,
        Long assignedUserId,
        String notes,
        Instant createdAt,
        Instant updatedAt
    ) {}

    @Builder
    public record PageResponse(
        List<Response> content,
        int page,
        int size,
        long totalElements,
        int totalPages
    ) {}
}

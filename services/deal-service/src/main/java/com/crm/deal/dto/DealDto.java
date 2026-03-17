package com.crm.deal.dto;

import com.crm.deal.enums.DealStage;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class DealDto {

    public record CreateRequest(
        @NotNull Long leadId,
        @NotBlank @Size(max = 200) String title,
        @NotNull @DecimalMin("0.0") BigDecimal value,
        @NotNull LocalDate expectedCloseDate,
        Long ownerId,
        @Min(0) @Max(100) Integer probability,
        String notes
    ) {}

    public record UpdateRequest(
        @Size(max = 200) String title,
        @DecimalMin("0.0") BigDecimal value,
        LocalDate expectedCloseDate,
        @Min(0) @Max(100) Integer probability,
        String notes
    ) {}

    public record StageUpdateRequest(
        @NotNull DealStage stage
    ) {}

    public record AssignRequest(
        @NotNull Long ownerId
    ) {}

    @Builder
    public record Response(
        Long id,
        Long leadId,
        String title,
        BigDecimal value,
        DealStage stage,
        Integer probability,
        LocalDate expectedCloseDate,
        Long ownerId,
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

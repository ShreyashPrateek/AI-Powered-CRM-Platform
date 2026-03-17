package com.crm.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class RevenueDto {

    public record Summary(
        BigDecimal totalRevenue,
        BigDecimal pipelineValue,
        BigDecimal avgDealValue,
        long       wonDeals,
        long       lostDeals,
        double     winRate
    ) {}

    public record PeriodRevenue(
        String    period,       // ISO date range label
        BigDecimal revenue
    ) {}

    public record MonthlyRevenue(
        String     month,       // YYYY-MM
        BigDecimal revenue
    ) {}

    public record OwnerRevenue(
        Long       ownerId,
        BigDecimal revenue
    ) {}

    public record StageBreakdown(
        String     stage,
        long       count,
        BigDecimal value
    ) {}

    public record Dashboard(
        Summary              summary,
        List<MonthlyRevenue> monthly,
        List<StageBreakdown> byStage,
        List<OwnerRevenue>   topOwners
    ) {}
}

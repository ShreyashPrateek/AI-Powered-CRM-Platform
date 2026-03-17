package com.crm.analytics.dto;

import java.math.BigDecimal;
import java.util.List;

public class SalesPerformanceDto {

    public record RepPerformance(
        Long       ownerId,
        long       totalDeals,
        long       wonDeals,
        long       lostDeals,
        BigDecimal revenue,
        double     winRate
    ) {}

    public record Dashboard(
        List<RepPerformance> leaderboard,
        String               periodFrom,
        String               periodTo
    ) {}
}

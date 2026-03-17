package com.crm.analytics.controller;

import com.crm.analytics.dto.*;
import com.crm.analytics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RevenueAnalyticsService  revenueService;
    private final LeadConversionService    conversionService;
    private final SalesPerformanceService  performanceService;
    private final DealProbabilityService   probabilityService;

    /**
     * GET /api/v1/analytics/revenue?months=12
     * Sales revenue dashboard — total, pipeline, monthly trend, stage breakdown, top owners.
     */
    @GetMapping("/revenue")
    public RevenueDto.Dashboard revenue(
            @RequestParam(defaultValue = "12") int months) {
        return revenueService.dashboard(months);
    }

    /**
     * GET /api/v1/analytics/lead-conversion?months=6
     * Lead conversion funnel — rates, monthly trend, per-owner breakdown.
     */
    @GetMapping("/lead-conversion")
    public LeadConversionDto.Dashboard leadConversion(
            @RequestParam(defaultValue = "6") int months) {
        return conversionService.dashboard(months);
    }

    /**
     * GET /api/v1/analytics/sales-performance?months=3
     * Sales rep leaderboard — deals, wins, losses, revenue, win rate.
     */
    @GetMapping("/sales-performance")
    public SalesPerformanceDto.Dashboard salesPerformance(
            @RequestParam(defaultValue = "3") int months) {
        return performanceService.dashboard(months);
    }

    /**
     * GET /api/v1/analytics/deal-probability?daysAhead=30
     * Deal success probability — by stage, closing soon, high-probability deals.
     */
    @GetMapping("/deal-probability")
    public DealProbabilityDto.Dashboard dealProbability(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return probabilityService.dashboard(daysAhead);
    }

    /**
     * GET /api/v1/analytics/deal-probability/owner/{ownerId}
     * Open deals for a specific rep ordered by probability descending.
     */
    @GetMapping("/deal-probability/owner/{ownerId}")
    public List<DealProbabilityDto.DealPrediction> dealProbabilityByOwner(
            @PathVariable Long ownerId) {
        return probabilityService.byOwner(ownerId);
    }
}

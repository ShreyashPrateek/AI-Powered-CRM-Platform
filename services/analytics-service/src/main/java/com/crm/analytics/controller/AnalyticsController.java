package com.crm.analytics.controller;

import com.crm.analytics.dto.*;
import com.crm.analytics.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final RevenueAnalyticsService  revenueService;
    private final LeadConversionService    conversionService;
    private final SalesPerformanceService  performanceService;
    private final DealProbabilityService   probabilityService;

    /**
     * GET /api/analytics/summary
     * Aggregated summary for the dashboard.
     */
    @GetMapping("/summary")
    public java.util.Map<String, Object> summary() {
        RevenueDto.Dashboard rev = revenueService.dashboard(12);
        LeadConversionDto.Dashboard leads = conversionService.dashboard(6);
        DealProbabilityDto.Dashboard deals = probabilityService.dashboard(30);

        java.util.List<java.util.Map<String, Object>> revenueByMonth = rev.monthly().stream()
            .map(m -> java.util.Map.<String, Object>of("month", m.month(), "revenue", m.revenue()))
            .toList();

        java.util.List<java.util.Map<String, Object>> leadsByStatus = leads.funnel().stream()
            .map(s -> java.util.Map.<String, Object>of("status", s.status(), "count", s.count(), "percentage", s.percentage()))
            .toList();

        java.util.List<java.util.Map<String, Object>> dealsByStage = deals.byStage().stream()
            .map(s -> java.util.Map.<String, Object>of("stage", s.stage(), "avgProbability", s.avgProbability()))
            .toList();

        return java.util.Map.of(
            "totalRevenue", rev.summary().totalRevenue(),
            "totalLeads", leads.summary().totalLeads(),
            "totalDeals", rev.summary().wonDeals() + rev.summary().lostDeals(),
            "conversionRate", leads.summary().conversionRate(),
            "revenueByMonth", revenueByMonth,
            "leadsByStatus", leadsByStatus,
            "dealsByStage", dealsByStage
        );
    }

    /**
     * GET /api/analytics/revenue?months=12
     * Sales revenue dashboard — total, pipeline, monthly trend, stage breakdown, top owners.
     */
    @GetMapping("/revenue")
    public RevenueDto.Dashboard revenue(
            @RequestParam(defaultValue = "12") int months) {
        return revenueService.dashboard(months);
    }

    /**
     * GET /api/analytics/lead-conversion?months=6
     * Lead conversion funnel — rates, monthly trend, per-owner breakdown.
     */
    @GetMapping("/lead-conversion")
    public LeadConversionDto.Dashboard leadConversion(
            @RequestParam(defaultValue = "6") int months) {
        return conversionService.dashboard(months);
    }

    /**
     * GET /api/analytics/sales-performance?months=3
     * Sales rep leaderboard — deals, wins, losses, revenue, win rate.
     */
    @GetMapping("/sales-performance")
    public SalesPerformanceDto.Dashboard salesPerformance(
            @RequestParam(defaultValue = "3") int months) {
        return performanceService.dashboard(months);
    }

    /**
     * GET /api/analytics/deal-probability?daysAhead=30
     * Deal success probability — by stage, closing soon, high-probability deals.
     */
    @GetMapping("/deal-probability")
    public DealProbabilityDto.Dashboard dealProbability(
            @RequestParam(defaultValue = "30") int daysAhead) {
        return probabilityService.dashboard(daysAhead);
    }

    /**
     * GET /api/analytics/deal-probability/owner/{ownerId}
     * Open deals for a specific rep ordered by probability descending.
     */
    @GetMapping("/deal-probability/owner/{ownerId}")
    public List<DealProbabilityDto.DealPrediction> dealProbabilityByOwner(
            @PathVariable Long ownerId) {
        return probabilityService.byOwner(ownerId);
    }
}

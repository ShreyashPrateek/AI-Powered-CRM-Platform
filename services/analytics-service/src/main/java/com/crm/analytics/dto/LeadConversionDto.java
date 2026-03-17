package com.crm.analytics.dto;

import java.util.List;

public class LeadConversionDto {

    public record Summary(
        long   totalLeads,
        long   qualifiedLeads,
        long   lostLeads,
        double conversionRate,   // qualifiedLeads / totalLeads * 100
        double lossRate
    ) {}

    public record StatusBreakdown(
        String status,
        long   count,
        double percentage
    ) {}

    public record MonthlyConversion(
        String month,
        long   total,
        long   qualified,
        double conversionRate
    ) {}

    public record OwnerConversion(
        Long   ownerId,
        long   total,
        long   qualified,
        long   lost,
        double conversionRate
    ) {}

    public record Dashboard(
        Summary                  summary,
        List<StatusBreakdown>    funnel,
        List<MonthlyConversion>  monthly,
        List<OwnerConversion>    byOwner
    ) {}
}

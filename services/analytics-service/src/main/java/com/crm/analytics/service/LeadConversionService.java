package com.crm.analytics.service;

import com.crm.analytics.dto.LeadConversionDto;
import com.crm.analytics.repository.LeadSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadConversionService {

    private final LeadSnapshotRepository leadRepo;

    @Cacheable("analytics:conversion")
    public LeadConversionDto.Dashboard dashboard(int monthsBack) {
        Instant from = Instant.now().minus(monthsBack * 30L, ChronoUnit.DAYS);
        Instant to   = Instant.now();

        long total     = leadRepo.totalLeads();
        long qualified = leadRepo.qualifiedLeads();
        long lost      = leadRepo.lostLeads();

        double conversionRate = rate(qualified, total);
        double lossRate       = rate(lost, total);

        LeadConversionDto.Summary summary = new LeadConversionDto.Summary(
            total, qualified, lost, conversionRate, lossRate
        );

        List<LeadConversionDto.StatusBreakdown> funnel = leadRepo.countByStatus().stream()
            .map(row -> new LeadConversionDto.StatusBreakdown(
                row[0].toString(),
                ((Number) row[1]).longValue(),
                rate(((Number) row[1]).longValue(), total)
            ))
            .toList();

        List<LeadConversionDto.MonthlyConversion> monthly = leadRepo.monthlyLeadConversion(from).stream()
            .map(row -> {
                long t = ((Number) row[1]).longValue();
                long q = ((Number) row[2]).longValue();
                return new LeadConversionDto.MonthlyConversion(
                    (String) row[0], t, q, rate(q, t)
                );
            })
            .toList();

        List<LeadConversionDto.OwnerConversion> byOwner = leadRepo.conversionByOwner(from, to).stream()
            .map(row -> {
                long t = ((Number) row[1]).longValue();
                long q = ((Number) row[2]).longValue();
                long l = ((Number) row[3]).longValue();
                return new LeadConversionDto.OwnerConversion(
                    ((Number) row[0]).longValue(), t, q, l, rate(q, t)
                );
            })
            .toList();

        return new LeadConversionDto.Dashboard(summary, funnel, monthly, byOwner);
    }

    private double rate(long numerator, long denominator) {
        if (denominator == 0) return 0.0;
        return BigDecimal.valueOf(numerator)
            .divide(BigDecimal.valueOf(denominator), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }
}

package com.crm.analytics.service;

import com.crm.analytics.dto.RevenueDto;
import com.crm.analytics.repository.DealSnapshotRepository;
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
public class RevenueAnalyticsService {

    private final DealSnapshotRepository dealRepo;

    @Cacheable("analytics:revenue")
    public RevenueDto.Dashboard dashboard(int monthsBack) {
        Instant from = Instant.now().minus(monthsBack * 30L, ChronoUnit.DAYS);
        Instant to   = Instant.now();

        return new RevenueDto.Dashboard(
            buildSummary(from, to),
            buildMonthly(from),
            buildByStage(),
            buildTopOwners(from, to)
        );
    }

    private RevenueDto.Summary buildSummary(Instant from, Instant to) {
        BigDecimal totalRevenue  = dealRepo.totalRevenue();
        BigDecimal pipelineValue = dealRepo.pipelineValue();
        BigDecimal avgDealValue  = dealRepo.avgWonDealValue();
        long won   = dealRepo.countWon();
        long lost  = dealRepo.countLost();
        long terminal = dealRepo.countTerminal();

        double winRate = terminal == 0 ? 0.0
            : BigDecimal.valueOf(won)
                .divide(BigDecimal.valueOf(terminal), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        return new RevenueDto.Summary(totalRevenue, pipelineValue, avgDealValue, won, lost, winRate);
    }

    private List<RevenueDto.MonthlyRevenue> buildMonthly(Instant from) {
        return dealRepo.monthlyRevenue(from).stream()
            .map(row -> new RevenueDto.MonthlyRevenue(
                (String) row[0],
                (BigDecimal) row[1]
            ))
            .toList();
    }

    private List<RevenueDto.StageBreakdown> buildByStage() {
        return dealRepo.dealCountAndValueByStage().stream()
            .map(row -> new RevenueDto.StageBreakdown(
                row[0].toString(),
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2]
            ))
            .toList();
    }

    private List<RevenueDto.OwnerRevenue> buildTopOwners(Instant from, Instant to) {
        return dealRepo.revenueByOwner(from, to).stream()
            .map(row -> new RevenueDto.OwnerRevenue(
                ((Number) row[0]).longValue(),
                (BigDecimal) row[1]
            ))
            .toList();
    }
}

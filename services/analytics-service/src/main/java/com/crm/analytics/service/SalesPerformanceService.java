package com.crm.analytics.service;

import com.crm.analytics.dto.SalesPerformanceDto;
import com.crm.analytics.repository.DealSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesPerformanceService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

    private final DealSnapshotRepository dealRepo;

    @Cacheable("analytics:performance")
    public SalesPerformanceDto.Dashboard dashboard(int monthsBack) {
        Instant from = Instant.now().minus(monthsBack * 30L, ChronoUnit.DAYS);
        Instant to   = Instant.now();

        List<SalesPerformanceDto.RepPerformance> leaderboard =
            dealRepo.salesPerformanceByOwner(from, to).stream()
                .map(row -> {
                    long total   = ((Number) row[1]).longValue();
                    long won     = ((Number) row[2]).longValue();
                    long lost    = ((Number) row[3]).longValue();
                    BigDecimal revenue = (BigDecimal) row[4];
                    double winRate = total == 0 ? 0.0
                        : BigDecimal.valueOf(won)
                            .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    return new SalesPerformanceDto.RepPerformance(
                        ((Number) row[0]).longValue(), total, won, lost, revenue, winRate
                    );
                })
                .toList();

        return new SalesPerformanceDto.Dashboard(leaderboard, FMT.format(from), FMT.format(to));
    }
}

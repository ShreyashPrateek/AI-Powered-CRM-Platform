package com.crm.analytics.service;

import com.crm.analytics.dto.DealProbabilityDto;
import com.crm.analytics.entity.DealSnapshot;
import com.crm.analytics.repository.DealSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DealProbabilityService {

    private final DealSnapshotRepository dealRepo;

    @Cacheable("analytics:probability")
    public DealProbabilityDto.Dashboard dashboard(int daysAhead) {
        LocalDate deadline = LocalDate.now().plusDays(daysAhead);

        List<DealProbabilityDto.StageProbability> byStage =
            dealRepo.avgProbabilityByStage().stream()
                .map(row -> new DealProbabilityDto.StageProbability(
                    row[0].toString(),
                    ((Number) row[1]).doubleValue()
                ))
                .toList();

        List<DealProbabilityDto.DealPrediction> closingSoon =
            dealRepo.dealsClosingSoon(deadline).stream()
                .map(this::toPrediction)
                .toList();

        // High probability = top 10 open deals across all owners
        List<DealProbabilityDto.DealPrediction> highProbability =
            dealRepo.findAll().stream()
                .filter(d -> !d.getStage().isTerminal())
                .sorted((a, b) -> Integer.compare(b.getProbability(), a.getProbability()))
                .limit(10)
                .map(this::toPrediction)
                .toList();

        return new DealProbabilityDto.Dashboard(byStage, closingSoon, highProbability);
    }

    @Cacheable(value = "analytics:probability", key = "'owner:' + #ownerId")
    public List<DealProbabilityDto.DealPrediction> byOwner(Long ownerId) {
        return dealRepo.openDealsByOwnerOrderedByProbability(ownerId).stream()
            .map(this::toPrediction)
            .toList();
    }

    private DealProbabilityDto.DealPrediction toPrediction(DealSnapshot d) {
        return new DealProbabilityDto.DealPrediction(
            d.getDealId(),
            d.getOwnerId(),
            d.getValue(),
            d.getStage().name(),
            d.getProbability(),
            d.getExpectedCloseDate(),
            riskLevel(d.getProbability())
        );
    }

    /**
     * Risk classification:
     *  HIGH   — probability < 30  (likely to be lost)
     *  MEDIUM — probability 30–69
     *  LOW    — probability >= 70 (likely to close)
     */
    private String riskLevel(int probability) {
        if (probability >= 70) return "LOW";
        if (probability >= 30) return "MEDIUM";
        return "HIGH";
    }
}

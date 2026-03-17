package com.crm.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DealProbabilityDto {

    public record DealPrediction(
        Long       dealId,
        Long       ownerId,
        BigDecimal value,
        String     stage,
        int        probability,
        LocalDate  expectedCloseDate,
        String     riskLevel          // HIGH / MEDIUM / LOW
    ) {}

    public record StageProbability(
        String stage,
        double avgProbability
    ) {}

    public record Dashboard(
        List<StageProbability> byStage,
        List<DealPrediction>   closingSoon,
        List<DealPrediction>   highProbability
    ) {}
}

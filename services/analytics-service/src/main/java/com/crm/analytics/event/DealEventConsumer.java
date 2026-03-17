package com.crm.analytics.event;

import com.crm.analytics.entity.DealSnapshot;
import com.crm.analytics.enums.DealStage;
import com.crm.analytics.repository.DealSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DealEventConsumer {

    private final DealSnapshotRepository repository;

    @KafkaListener(
        topics = "deal-events",
        groupId = "analytics-service",
        containerFactory = "dealKafkaListenerContainerFactory"
    )
    @Transactional
    @CacheEvict(value = {
        "analytics:revenue", "analytics:pipeline",
        "analytics:performance", "analytics:probability"
    }, allEntries = true)
    public void onDealEvent(DealEventMessage event) {
        log.debug("Analytics consuming DealEvent [{}] dealId={}", event.eventType(), event.dealId());

        switch (event.eventType()) {
            case "DEAL_DELETED" -> repository.deleteById(event.dealId());
            default             -> upsert(event);
        }
    }

    private void upsert(DealEventMessage event) {
        DealSnapshot snapshot = repository.findById(event.dealId())
            .orElseGet(() -> DealSnapshot.builder()
                .dealId(event.dealId())
                .createdAt(event.occurredAt() != null ? event.occurredAt() : Instant.now())
                .build());

        if (event.stage()   != null) snapshot.setStage(DealStage.valueOf(event.stage()));
        if (event.value()   != null) snapshot.setValue(event.value());
        if (event.ownerId() != null) snapshot.setOwnerId(event.ownerId());
        if (event.leadId()  != null) snapshot.setLeadId(event.leadId());

        // Sync default probability from stage when stage changes
        if (event.stage() != null && snapshot.getProbability() == null) {
            snapshot.setProbability(defaultProbability(DealStage.valueOf(event.stage())));
        }

        if (snapshot.getExpectedCloseDate() == null) {
            snapshot.setExpectedCloseDate(LocalDate.now().plusMonths(1));
        }

        snapshot.setUpdatedAt(event.occurredAt() != null ? event.occurredAt() : Instant.now());
        repository.save(snapshot);
    }

    private int defaultProbability(DealStage stage) {
        return switch (stage) {
            case LEAD        -> 10;
            case QUALIFIED   -> 25;
            case PROPOSAL    -> 50;
            case NEGOTIATION -> 75;
            case CLOSED_WON  -> 100;
            case CLOSED_LOST -> 0;
        };
    }
}

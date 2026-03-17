package com.crm.analytics.event;

import com.crm.analytics.entity.LeadSnapshot;
import com.crm.analytics.enums.LeadStatus;
import com.crm.analytics.repository.LeadSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadEventConsumer {

    private final LeadSnapshotRepository repository;

    @KafkaListener(
        topics = "lead-events",
        groupId = "analytics-service",
        containerFactory = "leadKafkaListenerContainerFactory"
    )
    @Transactional
    @CacheEvict(value = {"analytics:conversion", "analytics:performance"}, allEntries = true)
    public void onLeadEvent(LeadEventMessage event) {
        log.debug("Analytics consuming LeadEvent [{}] leadId={}", event.eventType(), event.leadId());

        switch (event.eventType()) {
            case "LEAD_DELETED" -> repository.deleteById(event.leadId());
            default             -> upsert(event);
        }
    }

    private void upsert(LeadEventMessage event) {
        LeadSnapshot snapshot = repository.findById(event.leadId())
            .orElseGet(() -> LeadSnapshot.builder()
                .leadId(event.leadId())
                .createdAt(event.occurredAt() != null ? event.occurredAt() : Instant.now())
                .build());

        if (event.status()         != null) snapshot.setStatus(LeadStatus.valueOf(event.status()));
        if (event.assignedUserId() != null) snapshot.setAssignedUserId(event.assignedUserId());

        if (snapshot.getStatus() == null) snapshot.setStatus(LeadStatus.NEW);

        snapshot.setUpdatedAt(event.occurredAt() != null ? event.occurredAt() : Instant.now());
        repository.save(snapshot);
    }
}

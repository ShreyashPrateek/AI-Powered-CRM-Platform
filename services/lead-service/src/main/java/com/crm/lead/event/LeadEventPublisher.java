package com.crm.lead.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadEventPublisher {

    private static final String TOPIC = "lead-events";

    private final KafkaTemplate<String, LeadEvent> kafkaTemplate;

    public void publish(LeadEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.leadId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish LeadEvent [{}] for leadId={}", event.eventType(), event.leadId(), ex);
                } else {
                    log.debug("Published LeadEvent [{}] for leadId={}", event.eventType(), event.leadId());
                }
            });
    }
}

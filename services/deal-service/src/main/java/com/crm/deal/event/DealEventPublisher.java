package com.crm.deal.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DealEventPublisher {

    private static final String TOPIC = "deal-events";

    private final KafkaTemplate<String, DealEvent> kafkaTemplate;

    public void publish(DealEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.dealId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish DealEvent [{}] for dealId={}", event.eventType(), event.dealId(), ex);
                } else {
                    log.debug("Published DealEvent [{}] for dealId={}", event.eventType(), event.dealId());
                }
            });
    }
}

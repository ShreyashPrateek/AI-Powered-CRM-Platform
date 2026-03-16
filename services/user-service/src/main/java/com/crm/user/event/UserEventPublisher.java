package com.crm.user.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void publish(UserEvent event) {
        kafkaTemplate.send(TOPIC, String.valueOf(event.authUserId()), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish UserEvent [{}] for authUserId={}", event.eventType(), event.authUserId(), ex);
                } else {
                    log.debug("Published UserEvent [{}] for authUserId={}", event.eventType(), event.authUserId());
                }
            });
    }
}

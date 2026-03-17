package com.crm.email.event;

import com.crm.email.dto.ReminderDto;
import com.crm.email.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class LeadEventConsumer {

    private final ReminderService reminderService;

    @KafkaListener(topics = "lead-events", groupId = "email-service")
    public void onLeadEvent(LeadEventMessage event) {
        log.debug("Received LeadEvent [{}] for leadId={}", event.eventType(), event.leadId());

        switch (event.eventType()) {
            case "LEAD_ASSIGNED" -> scheduleFollowUp(event, 1);   // 1 day after assignment
            case "LEAD_CREATED"  -> scheduleFollowUp(event, 3);   // 3 days after creation
            default -> { /* no-op for other event types */ }
        }
    }

    private void scheduleFollowUp(LeadEventMessage event, int daysDelay) {
        if (event.leadEmail() == null) return;

        reminderService.create(new ReminderDto.CreateRequest(
            event.leadId(),
            event.leadEmail(),
            null,
            event.assignedUserId(),
            "Following up on your enquiry",
            "Hi there,\n\nI wanted to follow up regarding your recent enquiry. "
                + "Please let us know if you have any questions or need further assistance.",
            Instant.now().plus(daysDelay, ChronoUnit.DAYS)
        ));
        log.info("Scheduled follow-up for leadId={} in {} day(s)", event.leadId(), daysDelay);
    }
}

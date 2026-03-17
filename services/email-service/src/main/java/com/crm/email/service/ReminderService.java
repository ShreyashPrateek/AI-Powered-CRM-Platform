package com.crm.email.service;

import com.crm.email.dto.ReminderDto;
import com.crm.email.entity.EmailLog;
import com.crm.email.entity.FollowUpReminder;
import com.crm.email.enums.EmailStatus;
import com.crm.email.enums.ReminderStatus;
import com.crm.email.exception.ResourceNotFoundException;
import com.crm.email.repository.EmailLogRepository;
import com.crm.email.repository.FollowUpReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReminderService {

    private final FollowUpReminderRepository reminderRepository;
    private final EmailLogRepository         logRepository;
    private final SmtpEmailSender           smtpSender;

    public List<ReminderDto.Response> findByLead(Long leadId) {
        return reminderRepository.findByLeadId(leadId).stream().map(this::toResponse).toList();
    }

    public List<ReminderDto.Response> findByAssignedUser(Long userId) {
        return reminderRepository.findByAssignedUserId(userId).stream().map(this::toResponse).toList();
    }

    public ReminderDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ReminderDto.Response create(ReminderDto.CreateRequest req) {
        FollowUpReminder reminder = FollowUpReminder.builder()
            .leadId(req.leadId())
            .leadEmail(req.leadEmail())
            .leadName(req.leadName())
            .assignedUserId(req.assignedUserId())
            .subject(req.subject())
            .body(req.body())
            .scheduledAt(req.scheduledAt())
            .build();
        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public ReminderDto.Response update(Long id, ReminderDto.UpdateRequest req) {
        FollowUpReminder reminder = getOrThrow(id);
        if (req.subject()     != null) reminder.setSubject(req.subject());
        if (req.body()        != null) reminder.setBody(req.body());
        if (req.scheduledAt() != null) reminder.setScheduledAt(req.scheduledAt());
        return toResponse(reminderRepository.save(reminder));
    }

    @Transactional
    public void cancel(Long id) {
        FollowUpReminder reminder = getOrThrow(id);
        reminder.setStatus(ReminderStatus.CANCELLED);
        reminderRepository.save(reminder);
    }

    /**
     * Dispatches a single reminder — called by the Quartz scheduler.
     */
    @Transactional
    public void dispatch(FollowUpReminder reminder) {
        EmailStatus result = EmailStatus.SENT;
        String failureReason = null;
        try {
            smtpSender.send(
                reminder.getLeadEmail(),
                reminder.getSubject(),
                "followup",
                Map.of(
                    "leadName", reminder.getLeadName() != null ? reminder.getLeadName() : "",
                    "body",     reminder.getBody(),
                    "subject",  reminder.getSubject()
                )
            );
            reminder.setSentAt(Instant.now());
            reminder.setStatus(ReminderStatus.SENT);
        } catch (Exception ex) {
            result = EmailStatus.FAILED;
            failureReason = ex.getMessage();
            log.error("Failed to send follow-up to {}: {}", reminder.getLeadEmail(), ex.getMessage());
        }

        reminderRepository.save(reminder);

        logRepository.save(EmailLog.builder()
            .recipientEmail(reminder.getLeadEmail())
            .subject(reminder.getSubject())
            .emailType("FOLLOW_UP")
            .referenceId(reminder.getId())
            .status(result)
            .failureReason(failureReason)
            .sentAt(result == EmailStatus.SENT ? Instant.now() : null)
            .build());
    }

    /**
     * Fetches all PENDING reminders due now and dispatches them.
     * Invoked by the Quartz job every minute.
     */
    @Transactional
    public void dispatchDueReminders() {
        List<FollowUpReminder> due = reminderRepository
            .findByStatusAndScheduledAtBefore(ReminderStatus.PENDING, Instant.now());
        log.info("Dispatching {} due follow-up reminders", due.size());
        due.forEach(this::dispatch);
    }

    private FollowUpReminder getOrThrow(Long id) {
        return reminderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Reminder not found: " + id));
    }

    private ReminderDto.Response toResponse(FollowUpReminder r) {
        return ReminderDto.Response.builder()
            .id(r.getId())
            .leadId(r.getLeadId())
            .leadEmail(r.getLeadEmail())
            .leadName(r.getLeadName())
            .assignedUserId(r.getAssignedUserId())
            .subject(r.getSubject())
            .body(r.getBody())
            .scheduledAt(r.getScheduledAt())
            .status(r.getStatus())
            .sentAt(r.getSentAt())
            .createdAt(r.getCreatedAt())
            .build();
    }
}

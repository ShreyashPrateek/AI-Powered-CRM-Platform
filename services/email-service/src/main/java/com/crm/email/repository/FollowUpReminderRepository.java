package com.crm.email.repository;

import com.crm.email.entity.FollowUpReminder;
import com.crm.email.enums.ReminderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface FollowUpReminderRepository extends JpaRepository<FollowUpReminder, Long> {
    List<FollowUpReminder> findByLeadId(Long leadId);
    List<FollowUpReminder> findByStatusAndScheduledAtBefore(ReminderStatus status, Instant now);
    List<FollowUpReminder> findByAssignedUserId(Long assignedUserId);
}

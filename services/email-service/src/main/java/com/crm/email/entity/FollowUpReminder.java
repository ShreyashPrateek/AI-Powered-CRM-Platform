package com.crm.email.entity;

import com.crm.email.enums.ReminderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
    name = "follow_up_reminders",
    indexes = {
        @Index(name = "idx_reminder_lead",      columnList = "lead_id"),
        @Index(name = "idx_reminder_status",    columnList = "status"),
        @Index(name = "idx_reminder_scheduled", columnList = "scheduled_at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FollowUpReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(name = "lead_email", nullable = false, length = 150)
    private String leadEmail;

    @Column(name = "lead_name", length = 150)
    private String leadName;

    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(nullable = false, length = 255)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReminderStatus status = ReminderStatus.PENDING;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}

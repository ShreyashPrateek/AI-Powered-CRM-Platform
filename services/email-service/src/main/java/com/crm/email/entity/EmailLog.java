package com.crm.email.entity;

import com.crm.email.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
    name = "email_logs",
    indexes = {
        @Index(name = "idx_log_recipient", columnList = "recipient_email"),
        @Index(name = "idx_log_status",    columnList = "status"),
        @Index(name = "idx_log_sent_at",   columnList = "sent_at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_email", nullable = false, length = 150)
    private String recipientEmail;

    @Column(nullable = false, length = 255)
    private String subject;

    // "CAMPAIGN" | "FOLLOW_UP" | "AI_REPLY"
    @Column(nullable = false, length = 30)
    private String emailType;

    // nullable — links back to campaign or reminder if applicable
    @Column(name = "reference_id")
    private Long referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmailStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private Instant sentAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}

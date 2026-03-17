package com.crm.analytics.entity;

import com.crm.analytics.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Analytics-owned projection of lead data.
 * Kept in sync via Kafka lead-events.
 */
@Entity
@Table(
    name = "lead_snapshots",
    indexes = {
        @Index(name = "idx_ls_status",      columnList = "status"),
        @Index(name = "idx_ls_assigned",    columnList = "assignedUserId"),
        @Index(name = "idx_ls_created_at",  columnList = "createdAt")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class LeadSnapshot {

    @Id
    private Long leadId;

    private Long assignedUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeadStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
}

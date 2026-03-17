package com.crm.analytics.entity;

import com.crm.analytics.enums.DealStage;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Analytics-owned projection of deal data.
 * Kept in sync via Kafka deal-events — never written to by the deal-service directly.
 */
@Entity
@Table(
    name = "deal_snapshots",
    indexes = {
        @Index(name = "idx_ds_owner_id",    columnList = "ownerId"),
        @Index(name = "idx_ds_stage",       columnList = "stage"),
        @Index(name = "idx_ds_created_at",  columnList = "createdAt"),
        @Index(name = "idx_ds_close_date",  columnList = "expectedCloseDate")
    }
)
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class DealSnapshot {

    @Id
    private Long dealId;

    private Long leadId;
    private Long ownerId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DealStage stage;

    @Column(nullable = false)
    private Integer probability;

    private LocalDate expectedCloseDate;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;
}

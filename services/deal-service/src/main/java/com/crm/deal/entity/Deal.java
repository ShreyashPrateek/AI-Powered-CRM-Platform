package com.crm.deal.entity;

import com.crm.deal.enums.DealStage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
    name = "deals",
    indexes = {
        @Index(name = "idx_deal_stage",        columnList = "stage"),
        @Index(name = "idx_deal_lead_id",      columnList = "lead_id"),
        @Index(name = "idx_deal_owner",        columnList = "owner_id"),
        @Index(name = "idx_deal_close_date",   columnList = "expected_close_date")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // References Lead.id in lead-service — no FK across services
    @Column(name = "lead_id", nullable = false)
    private Long leadId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DealStage stage = DealStage.LEAD;

    // 0–100
    @Column(nullable = false)
    @Builder.Default
    private Integer probability = 10;

    @Column(name = "expected_close_date", nullable = false)
    private LocalDate expectedCloseDate;

    // References UserProfile.id in user-service — no FK across services
    @Column(name = "owner_id")
    private Long ownerId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

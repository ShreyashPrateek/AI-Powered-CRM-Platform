package com.crm.lead.entity;

import com.crm.lead.enums.Industry;
import com.crm.lead.enums.LeadSource;
import com.crm.lead.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
    name = "leads",
    indexes = {
        @Index(name = "idx_lead_status",        columnList = "status"),
        @Index(name = "idx_lead_assigned_user", columnList = "assigned_user_id"),
        @Index(name = "idx_lead_email",         columnList = "email"),
        @Index(name = "idx_lead_created_at",    columnList = "created_at")
    }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 150)
    private String company;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private Industry industry;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LeadSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    // References UserProfile.id in user-service — no FK across services
    @Column(name = "assigned_user_id")
    private Long assignedUserId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

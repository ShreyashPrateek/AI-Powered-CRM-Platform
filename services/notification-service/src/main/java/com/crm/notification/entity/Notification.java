package com.crm.notification.entity;

import com.crm.notification.enums.NotificationStatus;
import com.crm.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user_id", columnList = "userId"),
    @Index(name = "idx_notif_status",  columnList = "status")
})
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Source event that triggered this notification, e.g. LEAD_CREATED */
    private String sourceEvent;

    /** Optional deep-link reference, e.g. /leads/42 */
    private String referenceUrl;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant sentAt;
    private Instant readAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (status == null) status = NotificationStatus.PENDING;
    }
}

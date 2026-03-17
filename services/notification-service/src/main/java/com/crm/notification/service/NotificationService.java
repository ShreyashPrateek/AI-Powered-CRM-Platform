package com.crm.notification.service;

import com.crm.notification.dto.NotificationDto;
import com.crm.notification.entity.Notification;
import com.crm.notification.enums.NotificationStatus;
import com.crm.notification.enums.NotificationType;
import com.crm.notification.repository.NotificationRepository;
import com.crm.notification.websocket.NotificationWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final EmailNotificationService emailService;
    private final SmsService smsService;
    private final NotificationWebSocketHandler wsHandler;

    /**
     * Central dispatch: persists the notification then routes to the correct channel(s).
     */
    @Transactional
    public Notification dispatch(Long userId, NotificationType type, String title,
                                 String message, String sourceEvent, String referenceUrl) {
        Notification notification = repository.save(
            Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .sourceEvent(sourceEvent)
                .referenceUrl(referenceUrl)
                .status(NotificationStatus.PENDING)
                .build()
        );

        try {
            switch (type) {
                case EMAIL    -> emailService.send(notification);
                case SMS      -> smsService.send(notification);
                case IN_APP   -> markSent(notification);
                case WEBSOCKET -> {
                    wsHandler.sendToUser(userId, toResponse(notification));
                    markSent(notification);
                }
            }
        } catch (Exception ex) {
            log.error("Failed to dispatch notification id={} type={}", notification.getId(), type, ex);
            notification.setStatus(NotificationStatus.FAILED);
            repository.save(notification);
        }

        return notification;
    }

    public Page<NotificationDto.Response> getForUser(Long userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    public NotificationDto.UnreadCountResponse unreadCount(Long userId) {
        return new NotificationDto.UnreadCountResponse(
            repository.countByUserIdAndStatus(userId, NotificationStatus.SENT)
        );
    }

    @Transactional
    public void markRead(Long id) {
        repository.findById(id).ifPresent(n -> {
            n.setStatus(NotificationStatus.READ);
            n.setReadAt(Instant.now());
            repository.save(n);
        });
    }

    @Transactional
    public int markAllRead(Long userId) {
        return repository.markAllReadByUserId(userId);
    }

    void markSent(Notification n) {
        n.setStatus(NotificationStatus.SENT);
        n.setSentAt(Instant.now());
        repository.save(n);
    }

    public NotificationDto.Response toResponse(Notification n) {
        return new NotificationDto.Response(
            n.getId(), n.getUserId(), n.getType(), n.getStatus(),
            n.getTitle(), n.getMessage(), n.getSourceEvent(),
            n.getReferenceUrl(), n.getCreatedAt(), n.getSentAt(), n.getReadAt()
        );
    }
}

package com.crm.notification.controller;

import com.crm.notification.dto.NotificationDto;
import com.crm.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /api/notifications?userId=1&page=0&size=20 */
    @GetMapping
    public Page<NotificationDto.Response> list(
            @RequestParam Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return notificationService.getForUser(userId, pageable);
    }

    /** GET /api/notifications/unread-count?userId=1 */
    @GetMapping("/unread-count")
    public NotificationDto.UnreadCountResponse unreadCount(@RequestParam Long userId) {
        return notificationService.unreadCount(userId);
    }

    /** PATCH /api/notifications/{id}/read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    /** PATCH /api/notifications/read-all?userId=1 */
    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestParam Long userId) {
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }
}

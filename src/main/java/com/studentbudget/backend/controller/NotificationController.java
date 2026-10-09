package com.studentbudget.backend.controller;

import com.studentbudget.backend.dto.NotificationResponse;
import com.studentbudget.backend.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService =
                notificationService;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>>
    getNotifications(
            Principal principal) {

        return ResponseEntity.ok(
                notificationService
                        .getNotifications(
                                principal.getName()
                        )
        );
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>>
    getUnreadCount(
            Principal principal) {

        long count =
                notificationService
                        .getUnreadCount(
                                principal.getName()
                        );

        return ResponseEntity.ok(
                Map.of(
                        "unreadCount",
                        count
                )
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse>
    markAsRead(
            @PathVariable Long notificationId,
            Principal principal) {

        return ResponseEntity.ok(
                notificationService
                        .markAsRead(
                                principal.getName(),
                                notificationId
                        )
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>>
    markAllAsRead(
            Principal principal) {

        notificationService
                .markAllAsRead(
                        principal.getName()
                );

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "All notifications marked as read"
                )
        );
    }
}
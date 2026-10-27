package com.logistics.controltower.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notifications;
    public NotificationController(NotificationService notifications) { this.notifications = notifications; }
    @GetMapping
    public Page<NotificationService.View> list(Authentication authentication, Pageable pageable) {
        return notifications.list(authentication.getName(), pageable);
    }
    @PatchMapping("/{id}/read")
    public NotificationService.View markRead(@PathVariable long id, Authentication authentication) {
        return notifications.markRead(id, authentication.getName());
    }
}

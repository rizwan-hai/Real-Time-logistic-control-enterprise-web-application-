package com.logistics.controltower.notifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final SimpMessagingTemplate messaging;

    public NotificationService(NotificationRepository notifications, SimpMessagingTemplate messaging) {
        this.notifications = notifications;
        this.messaging = messaging;
    }

    @Transactional
    public void notifyUser(String recipientEmail, String type, String message, String entityType, Long entityId) {
        if (recipientEmail == null || recipientEmail.isBlank()) return;
        NotificationEntity notification = notifications.save(new NotificationEntity(
                recipientEmail, type, message, entityType, entityId));
        messaging.convertAndSendToUser(recipientEmail, "/queue/notifications", View.from(notification));
    }

    @Transactional(readOnly = true)
    public Page<View> list(String email, Pageable pageable) {
        return notifications.findByRecipientEmailIgnoreCaseOrderByCreatedAtDesc(email, pageable)
                .map(View::from);
    }

    @Transactional
    public View markRead(long id, String email) {
        NotificationEntity notification = notifications.findById(id)
                .filter(item -> item.getRecipientEmail().equalsIgnoreCase(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.markRead();
        return View.from(notification);
    }

    public record View(Long id, String type, String message, String entityType, Long entityId,
                       boolean read, java.time.Instant createdAt) {
        static View from(NotificationEntity item) {
            return new View(item.getId(), item.getType(), item.getMessage(), item.getEntityType(),
                    item.getEntityId(), item.isRead(), item.getCreatedAt());
        }
    }
}

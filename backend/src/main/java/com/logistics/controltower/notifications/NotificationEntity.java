package com.logistics.controltower.notifications;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 254)
    private String recipientEmail;
    @Column(nullable = false, length = 60)
    private String type;
    @Column(nullable = false, length = 500)
    private String message;
    @Column(length = 60)
    private String entityType;
    private Long entityId;
    @Column(nullable = false)
    private boolean read;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected NotificationEntity() {}
    public NotificationEntity(String recipientEmail, String type, String message,
                              String entityType, Long entityId) {
        this.recipientEmail = recipientEmail;
        this.type = type;
        this.message = message;
        this.entityType = entityType;
        this.entityId = entityId;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public void markRead() { read = true; }
    public Long getId() { return id; }
    public String getRecipientEmail() { return recipientEmail; }
    public String getType() { return type; }
    public String getMessage() { return message; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }
}

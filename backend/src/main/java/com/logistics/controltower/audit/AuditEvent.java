package com.logistics.controltower.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 80)
    private String eventType;
    @Column(length = 254)
    private String actorEmail;
    @Column(nullable = false, length = 80)
    private String outcome;
    @Column(length = 80)
    private String targetType;
    private Long targetId;
    @Column(length = 80)
    private String correlationId;
    @Column(length = 1000)
    private String metadata;
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEvent() {}

    public AuditEvent(String eventType, String actorEmail, String outcome, String targetType,
                      Long targetId, String correlationId, String metadata) {
        this.eventType = eventType;
        this.actorEmail = actorEmail;
        this.outcome = outcome;
        this.targetType = targetType;
        this.targetId = targetId;
        this.correlationId = correlationId;
        this.metadata = metadata;
    }

    @PrePersist
    void onCreate() { occurredAt = Instant.now(); }

    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActorEmail() { return actorEmail; }
    public String getOutcome() { return outcome; }
    public String getTargetType() { return targetType; }
    public Long getTargetId() { return targetId; }
    public String getCorrelationId() { return correlationId; }
    public String getMetadata() { return metadata; }
    public Instant getOccurredAt() { return occurredAt; }
}

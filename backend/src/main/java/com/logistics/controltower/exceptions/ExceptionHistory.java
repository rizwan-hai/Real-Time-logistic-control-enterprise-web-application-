package com.logistics.controltower.exceptions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "exception_history")
public class ExceptionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "exception_id", nullable = false)
    private DeliveryException deliveryException;
    @Column(nullable = false, length = 80)
    private String eventType;
    @Column(nullable = false, length = 254)
    private String actorEmail;
    @Column(length = 2000)
    private String notes;
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected ExceptionHistory() {}
    public ExceptionHistory(DeliveryException deliveryException, String eventType, String actorEmail, String notes) {
        this.deliveryException = deliveryException;
        this.eventType = eventType;
        this.actorEmail = actorEmail;
        this.notes = notes;
    }
    @PrePersist
    void onCreate() { occurredAt = Instant.now(); }
    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getActorEmail() { return actorEmail; }
    public String getNotes() { return notes; }
    public Instant getOccurredAt() { return occurredAt; }
}

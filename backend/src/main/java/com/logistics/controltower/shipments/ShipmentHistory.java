package com.logistics.controltower.shipments;

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
@Table(name = "shipment_history")
public class ShipmentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;
    @Column(nullable = false, length = 30)
    private String status;
    @Column(nullable = false, length = 254)
    private String actorEmail;
    @Column(length = 1000)
    private String notes;
    @Column(nullable = false, updatable = false)
    private Instant occurredAt;

    protected ShipmentHistory() {}
    public ShipmentHistory(ShipmentEntity shipment, String status, String actorEmail, String notes) {
        this.shipment = shipment;
        this.status = status;
        this.actorEmail = actorEmail;
        this.notes = notes;
    }
    @PrePersist
    void onCreate() { occurredAt = Instant.now(); }
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public String getActorEmail() { return actorEmail; }
    public String getNotes() { return notes; }
    public Instant getOccurredAt() { return occurredAt; }
}

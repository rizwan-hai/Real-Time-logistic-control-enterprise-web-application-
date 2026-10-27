package com.logistics.controltower.exceptions;

import com.logistics.controltower.shipments.ShipmentEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;

@Entity
@Table(name = "delivery_exceptions")
public class DeliveryException {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    private ShipmentEntity shipment;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExceptionType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExceptionSeverity severity;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ExceptionStatus status;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(length = 254)
    private String assignedToEmail;
    @Column(nullable = false, length = 254)
    private String createdByEmail;
    @Column(length = 2000)
    private String resolutionNotes;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private Instant responseDeadline;
    private Instant resolvedAt;
    @Version
    private long version;

    protected DeliveryException() {}
    public DeliveryException(ShipmentEntity shipment, ExceptionType type, ExceptionSeverity severity,
                            String description, String assignedToEmail, String createdByEmail,
                            Instant responseDeadline) {
        this.shipment = shipment;
        this.type = type;
        this.severity = severity;
        this.status = ExceptionStatus.OPEN;
        this.description = description;
        this.assignedToEmail = assignedToEmail;
        this.createdByEmail = createdByEmail;
        this.responseDeadline = responseDeadline;
    }
    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public void setStatus(ExceptionStatus status, String resolutionNotes) {
        this.status = status;
        if (status == ExceptionStatus.RESOLVED) {
            this.resolvedAt = Instant.now();
            this.resolutionNotes = resolutionNotes;
        } else if (status == ExceptionStatus.IN_PROGRESS) {
            this.resolvedAt = null;
            this.resolutionNotes = null;
        }
    }
    public void setSeverity(ExceptionSeverity severity) { this.severity = severity; }
    public void setAssignedToEmail(String email) { this.assignedToEmail = email; }

    public Long getId() { return id; }
    public ShipmentEntity getShipment() { return shipment; }
    public ExceptionType getType() { return type; }
    public ExceptionSeverity getSeverity() { return severity; }
    public ExceptionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getAssignedToEmail() { return assignedToEmail; }
    public String getCreatedByEmail() { return createdByEmail; }
    public String getResolutionNotes() { return resolutionNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getResponseDeadline() { return responseDeadline; }
    public Instant getResolvedAt() { return resolvedAt; }
    public long getVersion() { return version; }
}

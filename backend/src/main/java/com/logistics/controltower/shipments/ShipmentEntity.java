package com.logistics.controltower.shipments;

import com.logistics.controltower.customers.CustomerEntity;
import com.logistics.controltower.drivers.DriverEntity;
import com.logistics.controltower.vehicles.VehicleEntity;
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
@Table(name = "shipments")
public class ShipmentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String trackingNumber;
    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerEntity customer;
    @Column(nullable = false, length = 250)
    private String origin;
    @Column(nullable = false, length = 250)
    private String destination;
    @Column(nullable = false, length = 500)
    private String deliveryAddress;
    @Column(length = 1000)
    private String deliveryInstructions;
    @Column(length = 1000)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentPriority priority;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShipmentStatus status;
    @ManyToOne
    @JoinColumn(name = "driver_id")
    private DriverEntity driver;
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private VehicleEntity vehicle;
    private Instant estimatedDeliveryAt;
    private Instant actualDeliveryAt;
    @Column(length = 1000)
    private String outcomeNotes;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Version
    private long version;

    protected ShipmentEntity() {}

    public ShipmentEntity(String trackingNumber, CustomerEntity customer, String origin, String destination,
                          String deliveryAddress, String deliveryInstructions, String description,
                          ShipmentPriority priority, Instant estimatedDeliveryAt) {
        this.trackingNumber = trackingNumber;
        this.customer = customer;
        this.origin = origin;
        this.destination = destination;
        this.deliveryAddress = deliveryAddress;
        this.deliveryInstructions = deliveryInstructions;
        this.description = description;
        this.priority = priority;
        this.estimatedDeliveryAt = estimatedDeliveryAt;
        this.status = ShipmentStatus.CREATED;
    }

    @PrePersist
    void onCreate() { createdAt = updatedAt = Instant.now(); }
    @PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public void assign(DriverEntity driver, VehicleEntity vehicle) {
        this.driver = driver;
        this.vehicle = vehicle;
        this.status = ShipmentStatus.ASSIGNED;
    }

    public void changeStatus(ShipmentStatus status, String notes) {
        this.status = status;
        if (status == ShipmentStatus.DELIVERED) actualDeliveryAt = Instant.now();
        if (notes != null) outcomeNotes = notes;
    }

    public Long getId() { return id; }
    public String getTrackingNumber() { return trackingNumber; }
    public CustomerEntity getCustomer() { return customer; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public String getDeliveryInstructions() { return deliveryInstructions; }
    public String getDescription() { return description; }
    public ShipmentPriority getPriority() { return priority; }
    public ShipmentStatus getStatus() { return status; }
    public DriverEntity getDriver() { return driver; }
    public VehicleEntity getVehicle() { return vehicle; }
    public Instant getEstimatedDeliveryAt() { return estimatedDeliveryAt; }
    public Instant getActualDeliveryAt() { return actualDeliveryAt; }
    public String getOutcomeNotes() { return outcomeNotes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}

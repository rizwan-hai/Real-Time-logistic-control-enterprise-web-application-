package com.logistics.controltower.vehicles;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicles")
public class VehicleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String registration;
    @Column(nullable = false, length = 60)
    private String type;
    @Column(nullable = false)
    private int capacity;
    @Column(nullable = false, length = 30)
    private String status;

    protected VehicleEntity() {}
    public VehicleEntity(String registration, String type, int capacity) {
        this.registration = registration;
        this.type = type;
        this.capacity = capacity;
        this.status = "AVAILABLE";
    }
    public void setStatus(String status) { this.status = status; }
    public Long getId() { return id; }
    public String getRegistration() { return registration; }
    public String getType() { return type; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
}

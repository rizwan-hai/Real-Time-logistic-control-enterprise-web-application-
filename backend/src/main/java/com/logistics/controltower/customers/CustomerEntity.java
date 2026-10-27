package com.logistics.controltower.customers;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 160)
    private String name;
    @Column(length = 254)
    private String email;
    @Column(length = 40)
    private String phone;
    @Column(nullable = false)
    private boolean active = true;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected CustomerEntity() {}
    public CustomerEntity(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
    public void update(String name, String email, String phone, boolean active) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }
    @PrePersist
    void onCreate() { createdAt = Instant.now(); }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
}

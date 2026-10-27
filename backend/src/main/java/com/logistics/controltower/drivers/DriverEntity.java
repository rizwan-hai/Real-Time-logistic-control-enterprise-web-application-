package com.logistics.controltower.drivers;

import com.logistics.controltower.users.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "drivers")
public class DriverEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;
    @Column(nullable = false, length = 40)
    private String licenseNumber;
    @Column(nullable = false, length = 30)
    private String status;

    protected DriverEntity() {}
    public DriverEntity(UserEntity user, String licenseNumber) {
        this.user = user;
        this.licenseNumber = licenseNumber;
        this.status = "AVAILABLE";
    }
    public void setStatus(String status) { this.status = status; }
    public Long getId() { return id; }
    public UserEntity getUser() { return user; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getStatus() { return status; }
}

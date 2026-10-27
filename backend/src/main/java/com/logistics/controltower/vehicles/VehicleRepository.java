package com.logistics.controltower.vehicles;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, Long> {
    long countByStatus(String status);
}

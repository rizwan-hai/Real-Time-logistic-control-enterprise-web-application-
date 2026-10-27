package com.logistics.controltower.drivers;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverRepository extends JpaRepository<DriverEntity, Long> {
    Optional<DriverEntity> findByUserId(Long userId);
    Page<DriverEntity> findAll(Pageable pageable);
    long countByStatus(String status);
}

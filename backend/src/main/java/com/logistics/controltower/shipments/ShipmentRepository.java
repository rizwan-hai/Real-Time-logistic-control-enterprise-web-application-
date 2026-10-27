package com.logistics.controltower.shipments;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShipmentRepository extends JpaRepository<ShipmentEntity, Long> {
    Optional<ShipmentEntity> findByTrackingNumberIgnoreCase(String trackingNumber);

    boolean existsByDriverIdAndStatusIn(Long driverId, Collection<ShipmentStatus> statuses);
    boolean existsByVehicleIdAndStatusIn(Long vehicleId, Collection<ShipmentStatus> statuses);
    long countByStatus(ShipmentStatus status);

    @Query("select count(s) from ShipmentEntity s where s.status in :statuses and s.estimatedDeliveryAt < :now")
    long countDelayed(@Param("statuses") Collection<ShipmentStatus> statuses, @Param("now") Instant now);

    @Query(value = "select avg(timestampdiff(second, created_at, actual_delivery_at)) "
            + "from shipments where actual_delivery_at is not null", nativeQuery = true)
    Double averageDeliverySeconds();

    @Query("""
            select s from ShipmentEntity s
            where (:status is null or s.status = :status)
              and (:priority is null or s.priority = :priority)
              and (:customerId is null or s.customer.id = :customerId)
              and (:driverId is null or s.driver.id = :driverId)
              and (:fromDate is null or s.createdAt >= :fromDate)
              and (:toDate is null or s.createdAt < :toDate)
              and (:tracking is null or lower(s.trackingNumber) like lower(concat('%', :tracking, '%')))
            """)
    Page<ShipmentEntity> search(@Param("status") ShipmentStatus status,
                                @Param("priority") ShipmentPriority priority,
                                @Param("customerId") Long customerId,
                                @Param("driverId") Long driverId,
                                @Param("fromDate") Instant fromDate,
                                @Param("toDate") Instant toDate,
                                @Param("tracking") String tracking,
                                Pageable pageable);

}

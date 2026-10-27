package com.logistics.controltower.shipments;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentHistoryRepository extends JpaRepository<ShipmentHistory, Long> {
    List<ShipmentHistory> findByShipmentIdOrderByOccurredAtAsc(Long shipmentId);
}

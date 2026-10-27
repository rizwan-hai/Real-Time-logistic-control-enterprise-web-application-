package com.logistics.controltower.dashboard;

import com.logistics.controltower.drivers.DriverRepository;
import com.logistics.controltower.exceptions.ExceptionService;
import com.logistics.controltower.security.Role;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.shipments.ShipmentStatus;
import com.logistics.controltower.vehicles.VehicleRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final ShipmentRepository shipments;
    private final ExceptionService exceptions;
    private final DriverRepository drivers;
    private final VehicleRepository vehicles;

    public DashboardService(ShipmentRepository shipments, ExceptionService exceptions,
                            DriverRepository drivers, VehicleRepository vehicles) {
        this.shipments = shipments;
        this.exceptions = exceptions;
        this.drivers = drivers;
        this.vehicles = vehicles;
    }

    public Summary summary() {
        Map<ShipmentStatus, Long> shipmentCounts = new EnumMap<>(ShipmentStatus.class);
        long total = 0;
        for (ShipmentStatus status : ShipmentStatus.values()) {
            long count = shipments.countByStatus(status);
            shipmentCounts.put(status, count);
            total += count;
        }
        long delivered = shipmentCounts.get(ShipmentStatus.DELIVERED);
        long failed = shipmentCounts.get(ShipmentStatus.FAILED_DELIVERY);
        long completed = delivered + failed;
        double successRate = completed == 0 ? 0 : (double) delivered / completed * 100.0;
        long delayed = shipments.countDelayed(List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY), Instant.now());
        return new Summary(total, Map.copyOf(shipmentCounts), delayed, failed, exceptions.countOpen(),
                exceptions.countOverdue(), exceptions.countHighSeverity(),
                drivers.countByStatus("AVAILABLE"), vehicles.countByStatus("AVAILABLE"),
                successRate, shipments.averageDeliverySeconds());
    }

    public record Summary(long totalShipments, Map<ShipmentStatus, Long> shipmentsByStatus,
                          long delayedShipments, long failedShipments, long openExceptions,
                          long overdueExceptions, long openHighOrCriticalExceptions,
                          long availableDrivers, long availableVehicles,
                          double deliverySuccessRatePercent, Double averageDeliverySeconds) {}
}

package com.logistics.controltower.vehicles;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.shipments.ShipmentStatus;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VehicleService {
    private final VehicleRepository vehicles;
    private final ShipmentRepository shipments;
    private final AuditService audit;
    public VehicleService(VehicleRepository vehicles, ShipmentRepository shipments, AuditService audit) {
        this.vehicles = vehicles;
        this.shipments = shipments;
        this.audit = audit;
    }
    @Transactional(readOnly = true)
    public Page<VehicleDtos.View> list(Pageable pageable) {
        return vehicles.findAll(pageable).map(VehicleDtos.View::from);
    }
    @Transactional
    public VehicleDtos.View create(VehicleDtos.CreateRequest request) {
        VehicleEntity vehicle = vehicles.save(new VehicleEntity(
                request.registration().trim().toUpperCase(), request.type(), request.capacity()));
        audit.record("VEHICLE_CREATED", "SUCCESS", "VEHICLE", vehicle.getId(), null);
        return VehicleDtos.View.from(vehicle);
    }
    @Transactional
    public VehicleDtos.View updateStatus(long id, String status) {
        if (!Set.of("AVAILABLE", "UNAVAILABLE", "IN_MAINTENANCE").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported vehicle status");
        }
        VehicleEntity vehicle = vehicles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
        if (!"AVAILABLE".equals(status) && shipments.existsByVehicleIdAndStatusIn(vehicle.getId(),
                List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle has an active delivery assignment");
        }
        vehicle.setStatus(status);
        audit.record("VEHICLE_STATUS_CHANGED", "SUCCESS", "VEHICLE", id, "status=" + status);
        return VehicleDtos.View.from(vehicle);
    }
}

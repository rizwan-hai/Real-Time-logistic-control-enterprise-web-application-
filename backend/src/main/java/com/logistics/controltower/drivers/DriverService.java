package com.logistics.controltower.drivers;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.security.Role;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.shipments.ShipmentStatus;
import com.logistics.controltower.users.UserEntity;
import com.logistics.controltower.users.UserRepository;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DriverService {
    private final DriverRepository drivers;
    private final UserRepository users;
    private final ShipmentRepository shipments;
    private final AuditService audit;
    public DriverService(DriverRepository drivers, UserRepository users,
                         ShipmentRepository shipments, AuditService audit) {
        this.drivers = drivers;
        this.users = users;
        this.shipments = shipments;
        this.audit = audit;
    }
    @Transactional(readOnly = true)
    public Page<DriverDtos.View> list(Pageable pageable) {
        return drivers.findAll(pageable).map(DriverDtos.View::from);
    }
    @Transactional
    public DriverDtos.View create(DriverDtos.CreateRequest request) {
        UserEntity user = users.findById(request.userId())
                .filter(UserEntity::isActive)
                .filter(candidate -> candidate.getRoles().contains(Role.DRIVER))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "An active user with the DRIVER role is required"));
        if (drivers.findByUserId(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already has a driver profile");
        }
        DriverEntity driver = drivers.save(new DriverEntity(user, request.licenseNumber()));
        audit.record("DRIVER_PROFILE_CREATED", "SUCCESS", "DRIVER", driver.getId(), null);
        return DriverDtos.View.from(driver);
    }
    @Transactional
    public DriverDtos.View updateStatus(long id, String status) {
        if (!Set.of("AVAILABLE", "UNAVAILABLE", "ON_LEAVE").contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported driver status");
        }
        DriverEntity driver = drivers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found"));
        if (!"AVAILABLE".equals(status) && shipments.existsByDriverIdAndStatusIn(driver.getId(),
                List.of(ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver has an active delivery assignment");
        }
        driver.setStatus(status);
        audit.record("DRIVER_STATUS_CHANGED", "SUCCESS", "DRIVER", id, "status=" + status);
        return DriverDtos.View.from(driver);
    }
}

package com.logistics.controltower.shipments;

import com.logistics.controltower.audit.AuditService;
import com.logistics.controltower.customers.CustomerEntity;
import com.logistics.controltower.customers.CustomerRepository;
import com.logistics.controltower.drivers.DriverEntity;
import com.logistics.controltower.drivers.DriverRepository;
import com.logistics.controltower.notifications.NotificationService;
import com.logistics.controltower.security.Role;
import com.logistics.controltower.users.UserEntity;
import com.logistics.controltower.users.UserRepository;
import com.logistics.controltower.vehicles.VehicleEntity;
import com.logistics.controltower.vehicles.VehicleRepository;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ShipmentService {
    private static final List<ShipmentStatus> ACTIVE_STATUSES = List.of(
            ShipmentStatus.ASSIGNED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY);
    private static final Map<ShipmentStatus, EnumSet<ShipmentStatus>> TRANSITIONS = transitions();

    private final ShipmentRepository shipments;
    private final ShipmentHistoryRepository history;
    private final CustomerRepository customers;
    private final DriverRepository drivers;
    private final VehicleRepository vehicles;
    private final UserRepository users;
    private final AuditService audit;
    private final NotificationService notifications;

    public ShipmentService(ShipmentRepository shipments, ShipmentHistoryRepository history,
                           CustomerRepository customers, DriverRepository drivers, VehicleRepository vehicles,
                           UserRepository users, AuditService audit, NotificationService notifications) {
        this.shipments = shipments;
        this.history = history;
        this.customers = customers;
        this.drivers = drivers;
        this.vehicles = vehicles;
        this.users = users;
        this.audit = audit;
        this.notifications = notifications;
    }

    @Transactional
    public ShipmentDtos.View create(ShipmentDtos.CreateRequest input, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        CustomerEntity customer = customers.findById(input.customerId())
                .filter(CustomerEntity::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Active customer not found"));
        String trackingNumber = "LGT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        ShipmentEntity shipment = shipments.save(new ShipmentEntity(trackingNumber, customer, input.origin(),
                input.destination(), input.deliveryAddress(), input.deliveryInstructions(), input.description(),
                input.priority(), input.estimatedDeliveryAt()));
        history.save(new ShipmentHistory(shipment, shipment.getStatus().name(), authentication.getName(), null));
        audit.record("SHIPMENT_CREATED", "SUCCESS", "SHIPMENT", shipment.getId(), null);
        return ShipmentDtos.View.from(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentDtos.View> search(ShipmentStatus status, ShipmentPriority priority, Long customerId,
                                          Long driverId, Instant fromDate, Instant toDate, String tracking,
                                          Pageable pageable, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER, Role.DRIVER, Role.VIEWER);
        if (fromDate != null && toDate != null && !fromDate.isBefore(toDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromDate must be before toDate");
        }
        if (hasRole(authentication, Role.DRIVER)) {
            long ownDriverId = getCurrentDriver(authentication).getId();
            if (driverId != null && driverId != ownDriverId) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Drivers may only view their own deliveries");
            }
            driverId = ownDriverId;
        }
        return shipments.search(status, priority, customerId, driverId, fromDate, toDate,
                tracking == null || tracking.isBlank() ? null : tracking.trim(), pageable)
                .map(ShipmentDtos.View::from);
    }

    @Transactional(readOnly = true)
    public ShipmentDtos.View get(long id, Authentication authentication) {
        ShipmentEntity shipment = find(id);
        authorizeRead(shipment, authentication);
        return ShipmentDtos.View.from(shipment);
    }

    @Transactional
    public ShipmentDtos.View assign(long id, ShipmentDtos.AssignmentRequest input, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        ShipmentEntity shipment = find(id);
        if (shipment.getStatus() != ShipmentStatus.READY_FOR_DISPATCH
                && shipment.getStatus() != ShipmentStatus.ASSIGNED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Shipment must be ready or already assigned");
        }
        DriverEntity driver = drivers.findById(input.driverId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver not found"));
        if (!driver.getUser().isActive() || !driver.getUser().getRoles().contains(Role.DRIVER)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Driver account is not active");
        }
        if (!"AVAILABLE".equals(driver.getStatus())
                || shipments.existsByDriverIdAndStatusIn(driver.getId(), ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Driver is unavailable");
        }
        VehicleEntity vehicle = vehicles.findById(input.vehicleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Vehicle not found"));
        if (!"AVAILABLE".equals(vehicle.getStatus())
                || shipments.existsByVehicleIdAndStatusIn(vehicle.getId(), ACTIVE_STATUSES)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Vehicle is unavailable");
        }
        shipment.assign(driver, vehicle);
        history.save(new ShipmentHistory(shipment, shipment.getStatus().name(), authentication.getName(),
                "Assigned driver and vehicle"));
        audit.record("SHIPMENT_ASSIGNED", "SUCCESS", "SHIPMENT", id,
                "driverId=" + driver.getId() + ";vehicleId=" + vehicle.getId());
        notifications.notifyUser(driver.getUser().getEmail(), "SHIPMENT_ASSIGNED",
                "A delivery has been assigned to you.", "SHIPMENT", shipment.getId());
        return ShipmentDtos.View.from(shipment);
    }

    @Transactional
    public ShipmentDtos.View changeStatus(long id, ShipmentDtos.StatusRequest input, Authentication authentication) {
        ShipmentEntity shipment = find(id);
        ShipmentStatus oldStatus = shipment.getStatus();
        ShipmentStatus newStatus = input.status();
        if (!TRANSITIONS.getOrDefault(oldStatus, EnumSet.noneOf(ShipmentStatus.class)).contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid shipment transition from " + oldStatus + " to " + newStatus);
        }
        authorizeTransition(shipment, newStatus, authentication);
        if ((newStatus == ShipmentStatus.DELIVERED || newStatus == ShipmentStatus.FAILED_DELIVERY)
                && (input.notes() == null || input.notes().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Delivery outcome notes are required");
        }
        shipment.changeStatus(newStatus, input.notes());
        history.save(new ShipmentHistory(shipment, newStatus.name(), authentication.getName(), input.notes()));
        audit.record("SHIPMENT_STATUS_CHANGED", "SUCCESS", "SHIPMENT", id,
                "from=" + oldStatus + ";to=" + newStatus);
        if (shipment.getDriver() != null) {
            notifications.notifyUser(shipment.getDriver().getUser().getEmail(), "SHIPMENT_STATUS_CHANGED",
                    "A delivery status has changed to " + newStatus + ".", "SHIPMENT", id);
        }
        return ShipmentDtos.View.from(shipment);
    }

    @Transactional(readOnly = true)
    public List<ShipmentDtos.HistoryView> history(long id, Authentication authentication) {
        ShipmentEntity shipment = find(id);
        authorizeRead(shipment, authentication);
        return history.findByShipmentIdOrderByOccurredAtAsc(id).stream()
                .map(ShipmentDtos.HistoryView::from).toList();
    }

    @Transactional(readOnly = true)
    public String export(ShipmentStatus status, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.SECURITY_AUDITOR);
        List<ShipmentEntity> rows = status == null ? shipments.findAll() : shipments.findAll().stream()
                .filter(row -> row.getStatus() == status).toList();
        StringBuilder csv = new StringBuilder("trackingNumber,status,priority,customer,origin,destination,createdAt\n");
        for (ShipmentEntity row : rows) {
            csv.append(csvCell(row.getTrackingNumber())).append(',')
                    .append(csvCell(row.getStatus().name())).append(',')
                    .append(csvCell(row.getPriority().name())).append(',')
                    .append(csvCell(row.getCustomer().getName())).append(',')
                    .append(csvCell(row.getOrigin())).append(',')
                    .append(csvCell(row.getDestination())).append(',')
                    .append(csvCell(row.getCreatedAt().toString())).append('\n');
        }
        audit.record("SHIPMENT_CSV_EXPORTED", "SUCCESS", "SHIPMENT", null, "rowCount=" + rows.size());
        return csv.toString();
    }

    private ShipmentEntity find(long id) {
        return shipments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shipment not found"));
    }

    private void authorizeRead(ShipmentEntity shipment, Authentication authentication) {
        requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER, Role.DRIVER, Role.VIEWER);
        if (hasRole(authentication, Role.DRIVER)
                && (shipment.getDriver() == null
                || !shipment.getDriver().getUser().getEmail().equalsIgnoreCase(authentication.getName()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shipment is not assigned to this driver");
        }
    }

    private void authorizeTransition(ShipmentEntity shipment, ShipmentStatus target, Authentication authentication) {
        if (target == ShipmentStatus.CANCELLED) {
            requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        } else if (target == ShipmentStatus.RETURNED || target == ShipmentStatus.READY_FOR_DISPATCH) {
            requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER);
        } else if (hasRole(authentication, Role.DRIVER)) {
            DriverEntity driver = getCurrentDriver(authentication);
            if (shipment.getDriver() == null || !shipment.getDriver().getId().equals(driver.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Shipment is not assigned to this driver");
            }
            if (!EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.OUT_FOR_DELIVERY,
                    ShipmentStatus.DELIVERED, ShipmentStatus.FAILED_DELIVERY).contains(target)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver cannot perform this status change");
            }
        } else {
            requireAnyRole(authentication, Role.ADMIN, Role.LOGISTICS_MANAGER, Role.DISPATCHER);
        }
    }

    private DriverEntity getCurrentDriver(Authentication authentication) {
        UserEntity user = users.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver account not found"));
        return drivers.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Driver profile not found"));
    }

    private boolean hasRole(Authentication authentication, Role role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }

    private void requireAnyRole(Authentication authentication, Role... roles) {
        for (Role role : roles) if (hasRole(authentication, role)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient role for this operation");
    }

    private static String csvCell(String value) {
        String safe = value == null ? "" : value;
        if (!safe.isEmpty() && "=+-@".indexOf(safe.charAt(0)) >= 0) safe = "'" + safe;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private static Map<ShipmentStatus, EnumSet<ShipmentStatus>> transitions() {
        Map<ShipmentStatus, EnumSet<ShipmentStatus>> map = new EnumMap<>(ShipmentStatus.class);
        map.put(ShipmentStatus.CREATED, EnumSet.of(ShipmentStatus.READY_FOR_DISPATCH, ShipmentStatus.CANCELLED));
        map.put(ShipmentStatus.READY_FOR_DISPATCH, EnumSet.of(ShipmentStatus.ASSIGNED, ShipmentStatus.CANCELLED));
        map.put(ShipmentStatus.ASSIGNED,
                EnumSet.of(ShipmentStatus.IN_TRANSIT, ShipmentStatus.CANCELLED, ShipmentStatus.READY_FOR_DISPATCH));
        map.put(ShipmentStatus.IN_TRANSIT,
                EnumSet.of(ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED, ShipmentStatus.FAILED_DELIVERY));
        map.put(ShipmentStatus.OUT_FOR_DELIVERY,
                EnumSet.of(ShipmentStatus.DELIVERED, ShipmentStatus.FAILED_DELIVERY));
        map.put(ShipmentStatus.FAILED_DELIVERY,
                EnumSet.of(ShipmentStatus.ASSIGNED, ShipmentStatus.RETURNED, ShipmentStatus.CANCELLED));
        map.put(ShipmentStatus.DELIVERED, EnumSet.of(ShipmentStatus.RETURNED));
        return map;
    }
}

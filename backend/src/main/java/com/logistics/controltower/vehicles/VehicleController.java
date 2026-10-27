package com.logistics.controltower.vehicles;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
    private final VehicleService vehicles;
    public VehicleController(VehicleService vehicles) { this.vehicles = vehicles; }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER','VIEWER')")
    public Page<VehicleDtos.View> list(Pageable pageable) { return vehicles.list(pageable); }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public VehicleDtos.View create(@Valid @RequestBody VehicleDtos.CreateRequest request) {
        return vehicles.create(request);
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public VehicleDtos.View updateStatus(@PathVariable long id,
                                         @Valid @RequestBody VehicleDtos.StatusRequest request) {
        return vehicles.updateStatus(id, request.status());
    }
}

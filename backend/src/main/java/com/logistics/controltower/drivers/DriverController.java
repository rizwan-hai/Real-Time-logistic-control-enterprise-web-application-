package com.logistics.controltower.drivers;

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
@RequestMapping("/api/v1/drivers")
public class DriverController {
    private final DriverService drivers;
    public DriverController(DriverService drivers) { this.drivers = drivers; }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER','VIEWER')")
    public Page<DriverDtos.View> list(Pageable pageable) { return drivers.list(pageable); }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public DriverDtos.View create(@Valid @RequestBody DriverDtos.CreateRequest request) {
        return drivers.create(request);
    }
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public DriverDtos.View updateStatus(@PathVariable long id,
                                       @Valid @RequestBody DriverDtos.StatusRequest request) {
        return drivers.updateStatus(id, request.status());
    }
}

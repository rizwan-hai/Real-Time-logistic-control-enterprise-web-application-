package com.logistics.controltower.shipments;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments")
public class ShipmentController {
    private final ShipmentService shipments;
    public ShipmentController(ShipmentService shipments) { this.shipments = shipments; }

    @GetMapping
    public Page<ShipmentDtos.View> search(
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) ShipmentPriority priority,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant toDate,
            @RequestParam(required = false) String tracking,
            Pageable pageable, Authentication authentication) {
        return shipments.search(status, priority, customerId, driverId, fromDate, toDate,
                tracking, pageable, authentication);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER')")
    public ShipmentDtos.View create(@Valid @RequestBody ShipmentDtos.CreateRequest request,
                                    Authentication authentication) {
        return shipments.create(request, authentication);
    }

    @GetMapping("/{id}")
    public ShipmentDtos.View get(@PathVariable long id, Authentication authentication) {
        return shipments.get(id, authentication);
    }

    @PutMapping("/{id}/assignment")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER')")
    public ShipmentDtos.View assign(@PathVariable long id,
                                    @Valid @RequestBody ShipmentDtos.AssignmentRequest request,
                                    Authentication authentication) {
        return shipments.assign(id, request, authentication);
    }

    @PutMapping("/{id}/status")
    public ShipmentDtos.View changeStatus(@PathVariable long id,
                                          @Valid @RequestBody ShipmentDtos.StatusRequest request,
                                          Authentication authentication) {
        return shipments.changeStatus(id, request, authentication);
    }

    @GetMapping("/{id}/history")
    public List<ShipmentDtos.HistoryView> history(@PathVariable long id, Authentication authentication) {
        return shipments.history(id, authentication);
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<String> export(@RequestParam(required = false) ShipmentStatus status,
                                         Authentication authentication) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=shipments.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(shipments.export(status, authentication));
    }
}

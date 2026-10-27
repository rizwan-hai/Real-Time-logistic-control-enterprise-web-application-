package com.logistics.controltower.dashboard;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {
    private final DashboardService dashboard;
    public DashboardController(DashboardService dashboard) { this.dashboard = dashboard; }
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER','VIEWER')")
    public DashboardService.Summary summary() { return dashboard.summary(); }
}

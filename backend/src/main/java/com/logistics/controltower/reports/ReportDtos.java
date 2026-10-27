package com.logistics.controltower.reports;

import java.time.Instant;

public final class ReportDtos {
    private ReportDtos() {}

    public record ShipmentPerformanceReport(long totalShipments, long delivered, long failed,
                                           double deliverySuccessRate, double averageDeliverySeconds) {}

    public record ExceptionReport(long open, long overdue, long highSeverity, long critical) {}

    public record DriverUtilizationReport(long activeDrivers, long availableDrivers, long totalDrivers) {}

    public record ReportSummary(ShipmentPerformanceReport shipmentPerformance,
                               ExceptionReport exceptionReport,
                               DriverUtilizationReport driverUtilization,
                               Instant generatedAt) {}
}

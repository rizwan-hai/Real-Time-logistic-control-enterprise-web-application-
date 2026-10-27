package com.logistics.controltower.reports;

import com.logistics.controltower.drivers.DriverRepository;
import com.logistics.controltower.exceptions.DeliveryExceptionRepository;
import com.logistics.controltower.exceptions.ExceptionSeverity;
import com.logistics.controltower.exceptions.ExceptionStatus;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.shipments.ShipmentStatus;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

@Service
public class ReportsService {
    private final ShipmentRepository shipments;
    private final DeliveryExceptionRepository exceptions;
    private final DriverRepository drivers;

    public ReportsService(ShipmentRepository shipments, DeliveryExceptionRepository exceptions, DriverRepository drivers) {
        this.shipments = shipments;
        this.exceptions = exceptions;
        this.drivers = drivers;
    }

    public ReportDtos.ReportSummary summary() {
        long total = shipments.findAll().size();
        long delivered = shipments.countByStatus(ShipmentStatus.DELIVERED);
        long failed = shipments.countByStatus(ShipmentStatus.FAILED_DELIVERY);
        double successRate = total == 0 ? 0 : (delivered / (double) Math.max(1, delivered + failed)) * 100.0;
        Double avgSec = shipments.averageDeliverySeconds();
        long open = exceptions.countByStatus(ExceptionStatus.OPEN);
        long overdue = exceptions.countByStatusInAndResponseDeadlineBefore(
                List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED), Instant.now());
        long highSeverity = exceptions.countBySeverityAndStatusIn(ExceptionSeverity.HIGH,
                List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED));
        long critical = exceptions.countBySeverityAndStatusIn(ExceptionSeverity.CRITICAL,
                List.of(ExceptionStatus.OPEN, ExceptionStatus.ACKNOWLEDGED, ExceptionStatus.IN_PROGRESS, ExceptionStatus.ESCALATED));
        long totalDrivers = drivers.count();
        long availableDrivers = drivers.countByStatus("AVAILABLE");
        return new ReportDtos.ReportSummary(
                new ReportDtos.ShipmentPerformanceReport(total, delivered, failed, successRate, avgSec == null ? 0 : avgSec),
                new ReportDtos.ExceptionReport(open, overdue, highSeverity, critical),
                new ReportDtos.DriverUtilizationReport(totalDrivers, availableDrivers, totalDrivers),
                Instant.now());
    }

    public String csvSummary() {
        var summary = summary();
        return "section,metric,value\n"
                + "shipment,totalShipments," + summary.shipmentPerformance().totalShipments() + "\n"
                + "shipment,delivered," + summary.shipmentPerformance().delivered() + "\n"
                + "shipment,failed," + summary.shipmentPerformance().failed() + "\n"
                + "shipment,deliverySuccessRate," + safeNumber(summary.shipmentPerformance().deliverySuccessRate()) + "\n"
                + "shipment,averageDeliverySeconds," + safeNumber(summary.shipmentPerformance().averageDeliverySeconds()) + "\n"
                + "exception,open," + summary.exceptionReport().open() + "\n"
                + "exception,overdue," + summary.exceptionReport().overdue() + "\n"
                + "exception,highSeverity," + summary.exceptionReport().highSeverity() + "\n"
                + "exception,critical," + summary.exceptionReport().critical() + "\n"
                + "driver,totalDrivers," + summary.driverUtilization().totalDrivers() + "\n"
                + "driver,availableDrivers," + summary.driverUtilization().availableDrivers() + "\n";
    }

    public byte[] pdfSummary() {
        var summary = summary();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.beginText();
                stream.newLineAtOffset(50, 750);
                stream.showText("Logistics Control Tower Report");
                stream.newLineAtOffset(0, -20);
                stream.showText("Generated: " + summary.generatedAt());
                stream.newLineAtOffset(0, -20);
                stream.showText("Total shipments: " + summary.shipmentPerformance().totalShipments());
                stream.newLineAtOffset(0, -20);
                stream.showText("Delivered: " + summary.shipmentPerformance().delivered());
                stream.newLineAtOffset(0, -20);
                stream.showText("Failed: " + summary.shipmentPerformance().failed());
                stream.newLineAtOffset(0, -20);
                stream.showText("Success rate: " + safeNumber(summary.shipmentPerformance().deliverySuccessRate()) + "%");
                stream.newLineAtOffset(0, -20);
                stream.showText("Open exceptions: " + summary.exceptionReport().open());
                stream.newLineAtOffset(0, -20);
                stream.showText("Overdue exceptions: " + summary.exceptionReport().overdue());
                stream.endText();
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            return output.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate PDF report", e);
        }
    }

    private String safeNumber(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}

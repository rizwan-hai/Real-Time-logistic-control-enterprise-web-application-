package com.logistics.controltower.reports;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportsController {
    private final ReportsService reports;

    public ReportsController(ReportsService reports) { this.reports = reports; }

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','SECURITY_AUDITOR','VIEWER')")
    public ReportDtos.ReportSummary overview() {
        return reports.summary();
    }

    @GetMapping(value = "/overview.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','SECURITY_AUDITOR','VIEWER')")
    public ResponseEntity<String> csvOverview() {
        String safe = reports.csvSummary();
        String encodedName = URLEncoder.encode("operations-report.csv", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(safe);
    }

    @GetMapping(value = "/overview.pdf", produces = "application/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','SECURITY_AUDITOR','VIEWER')")
    public ResponseEntity<byte[]> pdfOverview() {
        String encodedName = URLEncoder.encode("operations-report.pdf", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .contentType(MediaType.APPLICATION_PDF)
                .body(reports.pdfSummary());
    }
}

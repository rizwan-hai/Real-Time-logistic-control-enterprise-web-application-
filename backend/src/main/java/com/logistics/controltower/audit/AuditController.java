package com.logistics.controltower.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) { this.auditService = auditService; }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECURITY_AUDITOR')")
    public Page<AuditView> list(Pageable pageable) {
        return auditService.findAll(pageable).map(AuditView::from);
    }

    public record AuditView(Long id, String eventType, String actorEmail, String outcome,
                            String targetType, Long targetId, String correlationId,
                            String metadata, Instant occurredAt) {
        static AuditView from(AuditEvent event) {
            return new AuditView(event.getId(), event.getEventType(), event.getActorEmail(),
                    event.getOutcome(), event.getTargetType(), event.getTargetId(),
                    event.getCorrelationId(), event.getMetadata(), event.getOccurredAt());
        }
    }
}

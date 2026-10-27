package com.logistics.controltower.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class AuditService {
    private final AuditRepository repository;

    public AuditService(AuditRepository repository) {
        this.repository = repository;
    }

    public void record(String type, String outcome, String targetType, Long targetId, String metadata) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String actor = authentication == null || !authentication.isAuthenticated()
                ? null : authentication.getName();
        recordAs(actor, type, outcome, targetType, targetId, metadata);
    }

    public void recordAs(String actor, String type, String outcome, String targetType,
                         Long targetId, String metadata) {
        var attributes = RequestContextHolder.getRequestAttributes();
        String correlationId = attributes instanceof ServletRequestAttributes servletAttributes
                ? servletAttributes.getRequest().getHeader("X-Correlation-ID") : null;
        if (correlationId != null && correlationId.length() > 80) {
            correlationId = correlationId.substring(0, 80);
        }
        repository.save(new AuditEvent(type, actor, outcome, targetType, targetId, correlationId, metadata));
    }

    public Page<AuditEvent> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }
}

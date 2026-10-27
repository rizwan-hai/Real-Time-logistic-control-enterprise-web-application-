package com.logistics.controltower.exceptions;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExceptionEscalationScheduler {
    private final ExceptionService exceptions;

    public ExceptionEscalationScheduler(ExceptionService exceptions) {
        this.exceptions = exceptions;
    }

    @Scheduled(fixedDelayString = "${app.exceptions.escalation-scan-ms:60000}")
    public void escalateOverdue() {
        exceptions.escalateOverdue();
    }
}

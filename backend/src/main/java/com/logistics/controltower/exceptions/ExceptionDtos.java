package com.logistics.controltower.exceptions;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ExceptionDtos {
    private ExceptionDtos() {}

    public record CreateRequest(@NotNull Long shipmentId, @NotNull ExceptionType type,
                                @NotNull ExceptionSeverity severity,
                                @NotBlank @Size(max = 2000) String description,
                                @Email String assignedToEmail) {}
    public record StatusRequest(@NotNull ExceptionStatus status, @Size(max = 2000) String notes) {}
    public record SeverityRequest(@NotNull ExceptionSeverity severity) {}
    public record AssignmentRequest(@Email @NotBlank String assignedToEmail) {}
    public record NoteRequest(@NotBlank @Size(max = 2000) String notes) {}
    public record View(Long id, Long shipmentId, String trackingNumber, ExceptionType type,
                       ExceptionSeverity severity, ExceptionStatus status, String description,
                       String assignedToEmail, String createdByEmail, String resolutionNotes,
                       Instant createdAt, Instant responseDeadline, Instant resolvedAt) {
        static View from(DeliveryException exception) {
            return new View(exception.getId(), exception.getShipment().getId(),
                    exception.getShipment().getTrackingNumber(), exception.getType(),
                    exception.getSeverity(), exception.getStatus(), exception.getDescription(),
                    exception.getAssignedToEmail(), exception.getCreatedByEmail(),
                    exception.getResolutionNotes(), exception.getCreatedAt(),
                    exception.getResponseDeadline(), exception.getResolvedAt());
        }
    }
    public record HistoryView(Long id, String eventType, String actorEmail, String notes, Instant occurredAt) {
        static HistoryView from(ExceptionHistory history) {
            return new HistoryView(history.getId(), history.getEventType(), history.getActorEmail(),
                    history.getNotes(), history.getOccurredAt());
        }
    }
}

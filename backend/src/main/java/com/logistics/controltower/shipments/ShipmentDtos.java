package com.logistics.controltower.shipments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ShipmentDtos {
    private ShipmentDtos() {}

    public record CreateRequest(@NotNull Long customerId,
                                @NotBlank @Size(max = 250) String origin,
                                @NotBlank @Size(max = 250) String destination,
                                @NotBlank @Size(max = 500) String deliveryAddress,
                                @Size(max = 1000) String deliveryInstructions,
                                @Size(max = 1000) String description,
                                @NotNull ShipmentPriority priority,
                                Instant estimatedDeliveryAt) {}
    public record AssignmentRequest(@NotNull Long driverId, @NotNull Long vehicleId) {}
    public record StatusRequest(@NotNull ShipmentStatus status, @Size(max = 1000) String notes) {}
    public record View(Long id, String trackingNumber, Long customerId, String customerName,
                       String origin, String destination, String deliveryAddress,
                       String deliveryInstructions, String description, ShipmentPriority priority,
                       ShipmentStatus status, Long driverId, Long vehicleId,
                       Instant estimatedDeliveryAt, Instant actualDeliveryAt, String outcomeNotes,
                       Instant createdAt, Instant updatedAt, long version) {
        static View from(ShipmentEntity shipment) {
            return new View(shipment.getId(), shipment.getTrackingNumber(),
                    shipment.getCustomer().getId(), shipment.getCustomer().getName(),
                    shipment.getOrigin(), shipment.getDestination(), shipment.getDeliveryAddress(),
                    shipment.getDeliveryInstructions(), shipment.getDescription(), shipment.getPriority(),
                    shipment.getStatus(),
                    shipment.getDriver() == null ? null : shipment.getDriver().getId(),
                    shipment.getVehicle() == null ? null : shipment.getVehicle().getId(),
                    shipment.getEstimatedDeliveryAt(), shipment.getActualDeliveryAt(),
                    shipment.getOutcomeNotes(), shipment.getCreatedAt(), shipment.getUpdatedAt(),
                    shipment.getVersion());
        }
    }
    public record HistoryView(Long id, String status, String actorEmail, String notes, Instant occurredAt) {
        static HistoryView from(ShipmentHistory item) {
            return new HistoryView(item.getId(), item.getStatus(), item.getActorEmail(),
                    item.getNotes(), item.getOccurredAt());
        }
    }
}

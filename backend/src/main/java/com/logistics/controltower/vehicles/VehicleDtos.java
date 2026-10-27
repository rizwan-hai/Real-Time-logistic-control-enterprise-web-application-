package com.logistics.controltower.vehicles;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class VehicleDtos {
    private VehicleDtos() {}
    public record CreateRequest(@NotBlank @Size(max = 30) String registration,
                                @NotBlank @Size(max = 60) String type, @Min(1) int capacity) {}
    public record StatusRequest(@NotBlank String status) {}
    public record View(Long id, String registration, String type, int capacity, String status) {
        static View from(VehicleEntity vehicle) {
            return new View(vehicle.getId(), vehicle.getRegistration(), vehicle.getType(),
                    vehicle.getCapacity(), vehicle.getStatus());
        }
    }
}

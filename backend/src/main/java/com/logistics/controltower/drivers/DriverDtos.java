package com.logistics.controltower.drivers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class DriverDtos {
    private DriverDtos() {}
    public record CreateRequest(@NotNull Long userId, @NotBlank @Size(max = 40) String licenseNumber) {}
    public record StatusRequest(@NotBlank String status) {}
    public record View(Long id, Long userId, String displayName, String email,
                       String licenseNumber, String status) {
        static View from(DriverEntity driver) {
            return new View(driver.getId(), driver.getUser().getId(), driver.getUser().getDisplayName(),
                    driver.getUser().getEmail(), driver.getLicenseNumber(), driver.getStatus());
        }
    }
}

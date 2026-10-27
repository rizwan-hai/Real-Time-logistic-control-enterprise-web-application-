package com.logistics.controltower.customers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CustomerDtos {
    private CustomerDtos() {}
    public record Request(@NotBlank @Size(max = 160) String name,
                          @Email @Size(max = 254) String email,
                          @Size(max = 40) String phone,
                          boolean active) {}
    public record View(Long id, String name, String email, String phone, boolean active, Instant createdAt) {
        static View from(CustomerEntity customer) {
            return new View(customer.getId(), customer.getName(), customer.getEmail(),
                    customer.getPhone(), customer.isActive(), customer.getCreatedAt());
        }
    }
}

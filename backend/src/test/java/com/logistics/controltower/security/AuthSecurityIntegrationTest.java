package com.logistics.controltower.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.controltower.customers.CustomerEntity;
import com.logistics.controltower.customers.CustomerRepository;
import com.logistics.controltower.drivers.DriverEntity;
import com.logistics.controltower.drivers.DriverRepository;
import com.logistics.controltower.shipments.ShipmentEntity;
import com.logistics.controltower.shipments.ShipmentPriority;
import com.logistics.controltower.shipments.ShipmentRepository;
import com.logistics.controltower.shipments.ShipmentStatus;
import com.logistics.controltower.users.UserEntity;
import com.logistics.controltower.users.UserRepository;
import com.logistics.controltower.vehicles.VehicleEntity;
import com.logistics.controltower.vehicles.VehicleRepository;
import java.time.Instant;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityIntegrationTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository users;
    @Autowired CustomerRepository customers;
    @Autowired DriverRepository drivers;
    @Autowired VehicleRepository vehicles;
    @Autowired ShipmentRepository shipments;
    @Autowired PasswordEncoder passwordEncoder;

    private long unassignedShipmentId;

    @BeforeEach
    void setUp() {
        shipments.deleteAll();
        vehicles.deleteAll();
        drivers.deleteAll();
        customers.deleteAll();
        users.deleteAll();

        UserEntity admin = users.save(new UserEntity("admin@example.com", "Admin", passwordEncoder.encode("StrongPass123!"), Set.of(Role.ADMIN)));
        admin.setActive(true);
        users.save(admin);

        UserEntity driverUser = users.save(new UserEntity("driver@example.com", "Driver One", passwordEncoder.encode("StrongPass123!"), Set.of(Role.DRIVER)));
        DriverEntity driver = drivers.save(new DriverEntity(driverUser, "DL-12345"));
        driverUser.setActive(true);
        users.save(driverUser);

        UserEntity anotherDriver = users.save(new UserEntity("driver2@example.com", "Driver Two", passwordEncoder.encode("StrongPass123!"), Set.of(Role.DRIVER)));
        drivers.save(new DriverEntity(anotherDriver, "DL-67890"));
        anotherDriver.setActive(true);
        users.save(anotherDriver);

        CustomerEntity customer = customers.save(new CustomerEntity("Acme", "contact@acme.com", "555"));
        VehicleEntity vehicle = vehicles.save(new VehicleEntity("ABC-123", "Van", 2000));
        vehicle.setStatus("AVAILABLE");
        vehicles.save(vehicle);

        ShipmentEntity unassignedShipment = new ShipmentEntity("LGT-TEST123", customer, "A", "B", "Address", "Note", "Box",
                ShipmentPriority.HIGH, Instant.now().plusSeconds(3600));
        unassignedShipment.changeStatus(ShipmentStatus.CREATED, null);
        unassignedShipment = shipments.save(unassignedShipment);
        unassignedShipmentId = unassignedShipment.getId();

        ShipmentEntity assignedShipment = new ShipmentEntity("LGT-ASSIGNED1", customer, "A", "B", "Address", "Note", "Box",
                ShipmentPriority.HIGH, Instant.now().plusSeconds(3600));
        assignedShipment.assign(driver, vehicle);
        shipments.save(assignedShipment);
    }

    @Test
    void loginSucceedsAndDashboardRequiresAuth() throws Exception {
        String loginJson = "{\"email\":\"admin@example.com\",\"password\":\"StrongPass123!\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assert response.contains("accessToken");

        mockMvc.perform(get("/api/v1/dashboard/summary"))
                .andExpect(status().isForbidden());

        String token = response.split("\"accessToken\":\"")[1].split("\"", 2)[0];
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void driverCannotAccessUnassignedShipment() throws Exception {
        String loginJson = "{\"email\":\"driver@example.com\",\"password\":\"StrongPass123!\"}";
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = response.split("\"accessToken\":\"")[1].split("\"", 2)[0];

        mockMvc.perform(get("/api/v1/shipments/{id}", unassignedShipmentId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}

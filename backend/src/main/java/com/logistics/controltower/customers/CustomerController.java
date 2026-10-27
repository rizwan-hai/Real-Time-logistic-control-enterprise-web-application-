package com.logistics.controltower.customers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {
    private final CustomerService customers;
    public CustomerController(CustomerService customers) { this.customers = customers; }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER','DISPATCHER','VIEWER')")
    public Page<CustomerDtos.View> list(Pageable pageable) { return customers.list(pageable); }
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public CustomerDtos.View create(@Valid @RequestBody CustomerDtos.Request request) {
        return customers.create(request);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LOGISTICS_MANAGER')")
    public CustomerDtos.View update(@PathVariable long id, @Valid @RequestBody CustomerDtos.Request request) {
        return customers.update(id, request);
    }
}

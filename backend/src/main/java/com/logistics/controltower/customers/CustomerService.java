package com.logistics.controltower.customers;

import com.logistics.controltower.audit.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final AuditService audit;
    public CustomerService(CustomerRepository customers, AuditService audit) {
        this.customers = customers;
        this.audit = audit;
    }
    @Transactional(readOnly = true)
    public Page<CustomerDtos.View> list(Pageable pageable) {
        return customers.findAll(pageable).map(CustomerDtos.View::from);
    }
    @Transactional
    public CustomerDtos.View create(CustomerDtos.Request request) {
        CustomerEntity customer = customers.save(new CustomerEntity(request.name(), request.email(), request.phone()));
        audit.record("CUSTOMER_CREATED", "SUCCESS", "CUSTOMER", customer.getId(), null);
        return CustomerDtos.View.from(customer);
    }
    @Transactional
    public CustomerDtos.View update(long id, CustomerDtos.Request request) {
        CustomerEntity customer = customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        customer.update(request.name(), request.email(), request.phone(), request.active());
        audit.record("CUSTOMER_UPDATED", "SUCCESS", "CUSTOMER", id, null);
        return CustomerDtos.View.from(customer);
    }
}

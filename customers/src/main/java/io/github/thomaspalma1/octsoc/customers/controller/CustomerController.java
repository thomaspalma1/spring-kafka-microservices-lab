package io.github.thomaspalma1.octsoc.customers.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.thomaspalma1.octsoc.customers.model.Customer;
import io.github.thomaspalma1.octsoc.customers.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequiredArgsConstructor
@RequestMapping("customers")
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<Customer> save(@RequestBody Customer customer) {
        service.save(customer);
        return ResponseEntity.ok(customer);

    }

    @GetMapping("{id}")
    public ResponseEntity<Customer> getData(@PathVariable("id") Long id) {
        return service
                .findCustomer(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
    

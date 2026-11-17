package io.github.thomaspalma1.octsoc.customers.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import io.github.thomaspalma1.octsoc.customers.model.Customer;
import io.github.thomaspalma1.octsoc.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class CustomerService {

    private final CustomerRepository repository;

    public Customer save(Customer customer) {
        return repository.save(customer);
    }

    public Optional<Customer> findCustomer(Long costumerId) {
        return repository.findById(costumerId);
    }

}

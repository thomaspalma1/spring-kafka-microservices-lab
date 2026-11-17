package io.github.thomaspalma1.octsoc.customers.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.thomaspalma1.octsoc.customers.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long>{

}

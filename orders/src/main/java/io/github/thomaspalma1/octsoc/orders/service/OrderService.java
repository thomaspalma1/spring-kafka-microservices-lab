package io.github.thomaspalma1.octsoc.orders.service;

import org.springframework.stereotype.Service;

import io.github.thomaspalma1.octsoc.orders.repository.OrderItemRepository;
import io.github.thomaspalma1.octsoc.orders.repository.OrderRepository;
import io.github.thomaspalma1.octsoc.orders.validator.OrderValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class OrderService {
    
    private final OrderRepository repository;
    private final OrderItemRepository orderItemRepository;
    private final OrderValidator validator;
}

package io.github.thomaspalma1.octsoc.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.thomaspalma1.octsoc.orders.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}

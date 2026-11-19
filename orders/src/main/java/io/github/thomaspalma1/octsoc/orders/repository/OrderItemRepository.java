package io.github.thomaspalma1.octsoc.orders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.thomaspalma1.octsoc.orders.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

}

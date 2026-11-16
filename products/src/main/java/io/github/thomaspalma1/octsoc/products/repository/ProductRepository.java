package io.github.thomaspalma1.octsoc.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import io.github.thomaspalma1.octsoc.products.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}

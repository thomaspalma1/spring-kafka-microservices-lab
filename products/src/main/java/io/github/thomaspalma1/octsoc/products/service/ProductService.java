package io.github.thomaspalma1.octsoc.products.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import io.github.thomaspalma1.octsoc.products.model.Product;
import io.github.thomaspalma1.octsoc.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;

    public Product save(Product product) {
        return repository.save(product);
    }

    public Optional<Product> findProduct(Long productId) {
        return repository.findById(productId);
    }
}

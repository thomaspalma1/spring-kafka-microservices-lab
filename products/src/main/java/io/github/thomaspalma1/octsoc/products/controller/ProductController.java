package io.github.thomaspalma1.octsoc.products.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.thomaspalma1.octsoc.products.model.Product;
import io.github.thomaspalma1.octsoc.products.service.ProductService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("products")
public class ProductController {

    private final ProductService service;

    @PostMapping
    public ResponseEntity<Product> save(@RequestBody Product product) {
        service.save(product);
        return ResponseEntity.ok(product);
    }
    
    @GetMapping("{id}")
    public ResponseEntity<Product> getData (@PathVariable("id")Long id){
        return service
            .findProduct(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

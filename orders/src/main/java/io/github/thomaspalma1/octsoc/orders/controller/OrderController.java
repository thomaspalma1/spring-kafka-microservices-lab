package io.github.thomaspalma1.octsoc.orders.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.thomaspalma1.octsoc.orders.dto.OrderRequest;
import io.github.thomaspalma1.octsoc.orders.service.OrderService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    public ResponseEntity create(@RequestBody OrderRequest dto) {
        return null;

    }


}

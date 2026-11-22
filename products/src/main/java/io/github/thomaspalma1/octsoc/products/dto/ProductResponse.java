package io.github.thomaspalma1.octsoc.products.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        BigDecimal unitPrice) {

}

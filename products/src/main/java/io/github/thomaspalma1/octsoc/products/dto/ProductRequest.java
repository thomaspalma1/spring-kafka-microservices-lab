package io.github.thomaspalma1.octsoc.products.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProductRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        @Positive
        @Digits(integer = 14,
                fraction = 2)
        BigDecimal unitPrice) {

}

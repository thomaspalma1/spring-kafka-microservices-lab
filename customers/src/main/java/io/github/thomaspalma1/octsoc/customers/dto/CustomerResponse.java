package io.github.thomaspalma1.octsoc.customers.dto;

public record CustomerResponse(
        Long id,
        String name,
        String cpf,
        String street,
        String streetNumber,
        String neighborhood,
        String email,
        String phone) {

}

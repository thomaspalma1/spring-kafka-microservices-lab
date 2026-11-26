package io.github.thomaspalma1.octsoc.customers.dto;

import org.hibernate.validator.constraints.br.CPF;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank
        @Size(max = 150)
        String name,

        // The cpf column is char(11), so the formatted 000.000.000-00 that @CPF accepts is rejected
        @NotBlank
        @Pattern(regexp = "\\d{11}")
        @CPF
        String cpf,

        @Size(max = 100)
        String street,

        @Size(max = 10)
        String streetNumber,

        @Size(max = 100)
        String neighborhood,

        @Email
        @Size(max = 150)
        String email,

        @Size(max = 20)
        String phone) {

}

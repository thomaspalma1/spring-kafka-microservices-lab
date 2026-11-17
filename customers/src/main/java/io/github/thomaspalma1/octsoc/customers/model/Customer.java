package io.github.thomaspalma1.octsoc.customers.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.Id;

@Entity
@Data
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",
            length = 150,
            nullable = false)
    private String name;

    @Column(name = "cpf",
            length = 11,
            nullable = false)
    private String cpf;

    @Column(name = "street",
            length = 100)
    private String street;

    @Column(name = "street_number",
            length = 10)
    private String streetNumber;

    @Column(name = "neighborhood",
            length = 100)
    private String neighborhood;

    @Column(name = "email",
            length = 150)
    private String email;

    @Column(name = "phone",
            length = 20)
    private String phone;

}

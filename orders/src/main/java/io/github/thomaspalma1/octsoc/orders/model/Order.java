package io.github.thomaspalma1.octsoc.orders.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.github.thomaspalma1.octsoc.orders.enums.StatusOrder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id",
            nullable = false)
    private Long customerId;

    @Column(name = "order_date",
            nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "payment_key")
    private String paymentKey;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            length = 20)
    private StatusOrder status;

    @Column(name = "total",
            nullable = false,
            precision = 16,
            scale = 2)
    private BigDecimal total;

    @Column(name = "tracking_code")
    private String trackingCode;

    @Column(name = "invoice_url")
    private String invoiceUrl;

}

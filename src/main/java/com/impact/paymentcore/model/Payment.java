package com.impact.paymentcore.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.impact.paymentcore.exception.IllegalPaymentStateException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    private String id;
    private String accountId;
    private BigDecimal amount;
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    public static Payment createPending(String accountId, BigDecimal amount, LocalDate dueDate) {
        Payment payment = new Payment();
        payment.id = UUID.randomUUID().toString();
        payment.accountId = accountId;
        payment.amount = amount;
        payment.dueDate = dueDate;
        payment.status = PaymentStatus.PENDING;
        return payment;
    }

    public void advance() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalPaymentStateException(
                    "Payment " + id + " cannot be advanced — current status: " + status);
        }
        this.status = PaymentStatus.ADVANCED;
    }
}

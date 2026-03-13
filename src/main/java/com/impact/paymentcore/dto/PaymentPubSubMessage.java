package com.impact.paymentcore.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.impact.paymentcore.model.Payment;

import java.math.BigDecimal;

public record PaymentPubSubMessage(
        @JsonProperty("payment_id") String paymentId,
        @JsonProperty("account_id") String accountId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("due_date") String dueDate,
        @JsonProperty("status") String status

) {

    public static PaymentPubSubMessage fromPayment(Payment payment) {
        return new PaymentPubSubMessage(
                payment.getId(),
                payment.getAccountId(),
                payment.getAmount(),
                payment.getDueDate().toString(),
                payment.getStatus().name()
        );
    }
}

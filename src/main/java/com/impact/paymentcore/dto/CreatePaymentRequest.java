package com.impact.paymentcore.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePaymentRequest(
        String accountId,
        BigDecimal amount,
        LocalDate dueDate
) {
}

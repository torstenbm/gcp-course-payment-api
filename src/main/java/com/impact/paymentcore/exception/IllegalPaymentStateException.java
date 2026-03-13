package com.impact.paymentcore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IllegalPaymentStateException extends RuntimeException {

    public IllegalPaymentStateException(String message) {
        super(message);
    }
}

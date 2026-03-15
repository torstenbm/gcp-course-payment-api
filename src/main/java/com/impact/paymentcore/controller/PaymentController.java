package com.impact.paymentcore.controller;

import com.impact.paymentcore.dto.CreatePaymentRequest;
import com.impact.paymentcore.model.Payment;
import com.impact.paymentcore.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Payment Core API operations")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    @Operation(summary = "List all payments", description = "Returns all payments from the database")
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new payment", description = "Saves a PENDING payment to the database and publishes an event to Pub/Sub")
    public Payment createPayment(@RequestBody CreatePaymentRequest request) {
        return paymentService.createPayment(request);
    }

    @PostMapping("/{id}/advance")
    @Operation(summary = "Advance a payment", description = "Transitions a PENDING payment to ADVANCED status")
    public Payment advancePayment(@PathVariable String id) {
        return paymentService.advancePayment(id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete all payments", description = "Removes all payments from the database")
    public void deleteAllPayments() {
        paymentService.deleteAllPayments();
    }
}

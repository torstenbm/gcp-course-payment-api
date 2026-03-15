package com.impact.paymentcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.impact.paymentcore.dto.CreatePaymentRequest;
import com.impact.paymentcore.dto.PaymentPubSubMessage;
import com.impact.paymentcore.exception.PaymentNotFoundException;
import com.impact.paymentcore.model.Payment;
import com.impact.paymentcore.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;
    private final String topicName;

    public PaymentService(PaymentRepository paymentRepository,
                          PubSubTemplate pubSubTemplate,
                          ObjectMapper objectMapper,
                          @Value("${app.pubsub.new-payments-topic}") String topicName) {
        this.paymentRepository = paymentRepository;
        this.pubSubTemplate = pubSubTemplate;
        this.objectMapper = objectMapper;
        this.topicName = topicName;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Payment createPayment(CreatePaymentRequest request) {
        Payment payment = Payment.createPending(request.accountId(), request.amount(), request.dueDate());

        Payment saved = paymentRepository.save(payment);

        publishNewPaymentEvent(saved);

        return saved;
    }

    public Payment advancePayment(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        payment.advance();

        return paymentRepository.save(payment);
    }

    public void deleteAllPayments() {
        paymentRepository.deleteAll();
    }

    private void publishNewPaymentEvent(Payment payment) {
        PaymentPubSubMessage message = PaymentPubSubMessage.fromPayment(payment);

        try {
            String json = objectMapper.writeValueAsString(message);
            pubSubTemplate.publish(topicName, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize Pub/Sub message", e);
        }
    }
}

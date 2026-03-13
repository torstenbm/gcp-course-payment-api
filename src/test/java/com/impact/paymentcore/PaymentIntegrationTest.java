package com.impact.paymentcore;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.impact.paymentcore.dto.CreatePaymentRequest;
import com.impact.paymentcore.model.Payment;
import com.impact.paymentcore.model.PaymentStatus;
import com.impact.paymentcore.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PaymentIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @MockitoBean
    private PubSubTemplate pubSubTemplate;

    @AfterEach
    void cleanup() {
        paymentRepository.deleteAll();
    }

    @Test
    void getAllPayments_returnsListOfPayments() {
        paymentRepository.save(new Payment("id-1", "acc-1", new BigDecimal("100.00"), LocalDate.of(2026, 4, 12), PaymentStatus.PENDING));
        paymentRepository.save(new Payment("id-2", "acc-2", new BigDecimal("250.50"), LocalDate.of(2026, 5, 1), PaymentStatus.ADVANCED));

        ResponseEntity<Payment[]> response = restTemplate.getForEntity("/payments", Payment[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()[0].getId()).isEqualTo("id-1");
        assertThat(response.getBody()[0].getAccountId()).isEqualTo("acc-1");
        assertThat(response.getBody()[0].getAmount()).isEqualByComparingTo("100.00");
        assertThat(response.getBody()[0].getStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(response.getBody()[1].getId()).isEqualTo("id-2");
        assertThat(response.getBody()[1].getStatus()).isEqualTo(PaymentStatus.ADVANCED);
    }

    @Test
    void getAllPayments_returnsEmptyListWhenNoneExist() {
        ResponseEntity<Payment[]> response = restTemplate.getForEntity("/payments", Payment[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void createPayment_returns201WithSavedPayment() {
        CreatePaymentRequest request = new CreatePaymentRequest("acc-1", new BigDecimal("1000.00"), LocalDate.of(2026, 4, 12));

        ResponseEntity<Payment> response = restTemplate.postForEntity("/payments", request, Payment.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getAccountId()).isEqualTo("acc-1");
        assertThat(response.getBody().getAmount()).isEqualByComparingTo("1000.00");
        assertThat(response.getBody().getDueDate()).isEqualTo(LocalDate.of(2026, 4, 12));
        assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void advancePayment_returns200WithAdvancedPayment() {
        paymentRepository.save(new Payment("id-1", "acc-1", new BigDecimal("1000.00"), LocalDate.of(2026, 4, 12), PaymentStatus.PENDING));

        ResponseEntity<Payment> response = restTemplate.postForEntity("/payments/id-1/advance", null, Payment.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo("id-1");
        assertThat(response.getBody().getStatus()).isEqualTo(PaymentStatus.ADVANCED);
    }

    @Test
    void advancePayment_returns404WhenPaymentNotFound() {
        ResponseEntity<String> response = restTemplate.postForEntity("/payments/nonexistent/advance", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void openApiSpec_isServed() {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("\"openapi\"");
        assertThat(response.getBody()).contains("/payments");
        assertThat(response.getBody()).contains("/payments/{id}/advance");
    }

    @Test
    void swaggerUi_isAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui.html", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void advancePayment_returns409WhenPaymentNotPending() {
        paymentRepository.save(new Payment("id-1", "acc-1", new BigDecimal("1000.00"), LocalDate.of(2026, 4, 12), PaymentStatus.ADVANCED));

        ResponseEntity<String> response = restTemplate.postForEntity("/payments/id-1/advance", null, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}

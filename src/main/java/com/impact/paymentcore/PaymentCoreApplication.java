package com.impact.paymentcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// To run locally against Cloud SQL, authenticate with Google Cloud:
//   gcloud auth application-default login
//
// Configure via environment variables or application.yml:
//   GOOGLE_CLOUD_PROJECT, CLOUD_SQL_INSTANCE, DB_NAME, DB_USER, DB_PASSWORD
//
// Start the app:
//   ./gradlew bootRun
//
// Swagger UI will be available at: http://localhost:8080/swagger-ui.html

@SpringBootApplication
public class PaymentCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentCoreApplication.class, args);
    }
}

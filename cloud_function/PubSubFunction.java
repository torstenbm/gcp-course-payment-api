package gcfv2pubsub;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import io.cloudevents.CloudEvent;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.logging.Logger;

public class PubSubFunction implements CloudEventsFunction {
    private static final Logger logger = Logger.getLogger(PubSubFunction.class.getName());
    private static final Gson gson = new Gson();
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String BASE_URL = "BASE_URL_OF_CLOUD_RUN_CONTAINER";

    // CHANGED: Record converted to Static Class for GSON 2.8.8 compatibility
    public static class PaymentPubSubMessage {
        @SerializedName("payment_id") public String paymentId;
        @SerializedName("account_id") public String accountId;
        @SerializedName("amount") public BigDecimal amount;
        @SerializedName("due_date") public String dueDate;
        @SerializedName("status") public String status;
    }

    @Override
    public void accept(CloudEvent event) throws Exception {
        String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
        JsonObject payload = gson.fromJson(cloudEventData, JsonObject.class);
        
        String base64Data = payload.getAsJsonObject("message").get("data").getAsString();
        String decodedJson = new String(Base64.getDecoder().decode(base64Data), StandardCharsets.UTF_8);

        // Map to the Class instead of the Record
        PaymentPubSubMessage payment = gson.fromJson(decodedJson, PaymentPubSubMessage.class);
        
        // Logic check (Updated to use field access instead of record methods)
        if (payment == null || payment.paymentId == null || !shouldAdvance(payment)) {
            logger.info("Payment skipped or malformed.");
            return;
        }

        String fullUrl = String.format("%s/%s/advance", BASE_URL, payment.paymentId);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        logger.info("Advancing payment ID: " + payment.paymentId);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            logger.info("Success for " + payment.paymentId);
        } else {
            logger.severe("API Error: " + response.statusCode());
            throw new RuntimeException("Downstream failure");
        }
    }

    private boolean shouldAdvance(PaymentPubSubMessage payment) {
        // 1. Safety check: If amount is null, we can't compare it
        if (payment.amount == null) {
            logger.warning("Payment " + payment.paymentId + " has no amount. Skipping.");
            return false;
        }

        // 2. Define our boundaries
        BigDecimal min = new BigDecimal("10");
        BigDecimal max = new BigDecimal("10000");

        // 3. compareTo returns:
        // -1 if less than, 0 if equal, 1 if greater than
        boolean isAtLeastMin = payment.amount.compareTo(min) >= 0;
        boolean isAtMostMax = payment.amount.compareTo(max) <= 0;

        return isAtLeastMin && isAtMostMax;
    }
}

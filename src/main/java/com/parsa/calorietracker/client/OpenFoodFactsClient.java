package com.parsa.calorietracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.parsa.calorietracker.calc.CalorieCalculator;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OpenFoodFactsClient {

    private static final Logger log = LoggerFactory.getLogger(OpenFoodFactsClient.class);
    private static final BigDecimal KJ_TO_KCAL = new BigDecimal("4.184");
    private static final long MIN_INTERVAL_MS = 6000L; // 10 req/min

    private final RestClient restClient;
    private final String userAgent;
    private final AtomicLong lastRequestAt = new AtomicLong(0);

    public OpenFoodFactsClient(RestClient.Builder restClientBuilder,
                               @Value("${open-food-facts.base-url}") String baseUrl,
                               @Value("${open-food-facts.user-agent}") String userAgent) {
        this.userAgent = userAgent;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public Optional<Food> fetchByBarcode(String barcode) {
        rateLimit();
        JsonNode root;
        try {
            root = getWithRetry("/api/v2/product/" + barcode + ".json");
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                return Optional.empty();
            }
            throw ex;
        }
        if (root.path("status").asInt(0) != 1) {
            return Optional.empty();
        }
        return mapProduct(barcode, root.path("product"));
    }

    Optional<Food> mapProduct(String barcode, JsonNode product) {
        JsonNode nutriments = product.path("nutriments");
        BigDecimal kcal = decimal(nutriments.path("energy-kcal_100g").asText(null));
        if (kcal == null) {
            BigDecimal kj = decimal(nutriments.path("energy-kj_100g").asText(null));
            if (kj != null) {
                kcal = kj.divide(KJ_TO_KCAL, 4, RoundingMode.HALF_UP);
            }
        }
        if (kcal == null) {
            log.warn("OFF product missing energy: source=OPEN_FOOD_FACTS sourceId={}", barcode);
            return Optional.empty();
        }

        BigDecimal protein = nullSafe(decimal(nutriments.path("proteins_100g").asText(null)));
        BigDecimal carb = nullSafe(decimal(nutriments.path("carbohydrates_100g").asText(null)));
        BigDecimal fat = nullSafe(decimal(nutriments.path("fat_100g").asText(null)));
        BigDecimal fiber = decimal(nutriments.path("fiber_100g").asText(null));

        String name = firstNonBlank(
                product.path("product_name").asText(null),
                product.path("generic_name").asText(null),
                barcode
        );

        Food food = new Food();
        food.setName(name);
        food.setNameFa(name);
        food.setKcalPer100g(kcal);
        food.setProteinPer100g(protein);
        food.setCarbPer100g(carb);
        food.setFatPer100g(fat);
        food.setFiberPer100g(fiber);
        food.setState(FoodState.PACKAGED);
        food.setEdiblePortionPct(new BigDecimal("100"));
        food.setSource(FoodSource.OPEN_FOOD_FACTS);
        food.setSourceId(barcode);
        food.setLicense("ODbL-1.0");
        food.setFetchedAt(Instant.now());

        if (!CalorieCalculator.macrosConsistent(food)) {
            log.warn("Skipping OFF product failing macrosConsistent: source=OPEN_FOOD_FACTS sourceId={}", barcode);
            return Optional.empty();
        }
        log.info("Mapped OFF product source=OPEN_FOOD_FACTS sourceId={}", barcode);
        return Optional.of(food);
    }

    private JsonNode getWithRetry(String path) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                return restClient.get()
                        .uri(path)
                        .header("User-Agent", userAgent)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(JsonNode.class);
            } catch (RestClientResponseException ex) {
                int status = ex.getStatusCode().value();
                if (status >= 500 && attempts < 2) {
                    sleep(500L);
                    continue;
                }
                throw ex;
            }
        }
    }

    private void rateLimit() {
        long now = System.currentTimeMillis();
        long last = lastRequestAt.get();
        long wait = MIN_INTERVAL_MS - (now - last);
        if (wait > 0) {
            sleep(wait);
        }
        lastRequestAt.set(System.currentTimeMillis());
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static BigDecimal decimal(String raw) {
        if (raw == null || raw.isBlank() || "null".equalsIgnoreCase(raw)) {
            return null;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static BigDecimal nullSafe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "Unknown";
    }
}

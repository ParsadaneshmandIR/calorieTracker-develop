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
import org.springframework.web.util.UriBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@Component
public class UsdaFoodDataClient {

    private static final Logger log = LoggerFactory.getLogger(UsdaFoodDataClient.class);

    private final RestClient restClient;
    private final String apiKey;

    public UsdaFoodDataClient(RestClient.Builder restClientBuilder,
                              @Value("${usda.base-url}") String baseUrl,
                              @Value("${usda.api-key:}") String apiKey) {
        this.apiKey = apiKey == null ? "" : apiKey;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public List<Food> searchAndFetch(String query) {
        requireApiKey();
        JsonNode search = getWithRetry(uriBuilder -> uriBuilder
                .path("/foods/search")
                .queryParam("query", query)
                .queryParam("dataType", "Foundation", "SR Legacy")
                .queryParam("pageSize", 50)
                .queryParam("api_key", apiKey)
                .build());

        List<Food> imported = new ArrayList<>();
        JsonNode foods = search.path("foods");
        if (!foods.isArray()) {
            return imported;
        }
        for (JsonNode hit : foods) {
            long fdcId = hit.path("fdcId").asLong(0);
            if (fdcId == 0) {
                continue;
            }
            try {
                fetchByFdcId(fdcId).ifPresent(imported::add);
            } catch (Exception ex) {
                log.warn("Failed to import USDA fdcId={}: {}", fdcId, ex.getMessage());
            }
        }
        return imported;
    }

    public Optional<Food> fetchByFdcId(long fdcId) {
        requireApiKey();
        JsonNode detail = getWithRetry(uriBuilder -> uriBuilder
                .path("/food/{fdcId}")
                .queryParam("api_key", apiKey)
                .build(fdcId));
        return mapFood(detail);
    }

    Optional<Food> mapFood(JsonNode detail) {
        String fdcId = detail.path("fdcId").asText(null);
        if (fdcId == null || fdcId.isBlank()) {
            return Optional.empty();
        }
        String description = detail.path("description").asText("Unknown");

        BigDecimal kcal = null;
        BigDecimal protein = BigDecimal.ZERO;
        BigDecimal carb = BigDecimal.ZERO;
        BigDecimal fat = BigDecimal.ZERO;
        BigDecimal fiber = null;

        JsonNode nutrients = detail.path("foodNutrients");
        if (nutrients.isArray()) {
            for (JsonNode n : nutrients) {
                JsonNode nutrient = n.has("nutrient") ? n.get("nutrient") : n;
                int nutrientId = nutrient.path("id").asInt(nutrient.path("nutrientId").asInt(0));
                String nutrientName = nutrient.path("name").asText(n.path("nutrientName").asText("")).toLowerCase(Locale.ROOT);
                BigDecimal amount = decimal(n.path("amount").asText(n.path("value").asText(null)));
                if (amount == null) {
                    continue;
                }
                if (nutrientId == 1008 || (nutrientName.contains("energy") && nutrientName.contains("kcal"))) {
                    kcal = amount;
                } else if (nutrientId == 1003 || "protein".equals(nutrientName)) {
                    protein = amount;
                } else if (nutrientId == 1005 || nutrientName.contains("carbohydrate")) {
                    carb = amount;
                } else if (nutrientId == 1004 || nutrientName.contains("total lipid") || "fat".equals(nutrientName)) {
                    fat = amount;
                } else if (nutrientId == 1079 || nutrientName.contains("fiber")) {
                    fiber = amount;
                }
            }
        }

        if (kcal == null) {
            log.warn("Skipping USDA food without kcal: source=USDA sourceId={}", fdcId);
            return Optional.empty();
        }

        Food food = new Food();
        food.setName(description);
        food.setNameFa(description);
        food.setKcalPer100g(kcal);
        food.setProteinPer100g(protein);
        food.setCarbPer100g(carb);
        food.setFatPer100g(fat);
        food.setFiberPer100g(fiber);
        food.setState(FoodState.RAW);
        food.setEdiblePortionPct(new BigDecimal("100"));
        food.setSource(FoodSource.USDA);
        food.setSourceId(fdcId);
        food.setLicense("USDA public domain");
        food.setFetchedAt(Instant.now());

        if (!CalorieCalculator.macrosConsistent(food)) {
            log.warn("Skipping USDA food failing macrosConsistent: source=USDA sourceId={}", fdcId);
            return Optional.empty();
        }
        log.info("Mapped USDA food source=USDA sourceId={}", fdcId);
        return Optional.of(food);
    }

    private JsonNode getWithRetry(Function<UriBuilder, URI> uriFunction) {
        int attempts = 0;
        while (true) {
            attempts++;
            try {
                return restClient.get()
                        .uri(uriFunction)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .body(JsonNode.class);
            } catch (RestClientResponseException ex) {
                int status = ex.getStatusCode().value();
                if (status >= 500 && attempts < 2) {
                    sleepBackoff();
                    continue;
                }
                throw ex;
            }
        }
    }

    private void requireApiKey() {
        if (apiKey.isBlank()) {
            throw new IllegalStateException("USDA_API_KEY / usda.api-key is not configured");
        }
    }

    private static void sleepBackoff() {
        try {
            Thread.sleep(500L);
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
}

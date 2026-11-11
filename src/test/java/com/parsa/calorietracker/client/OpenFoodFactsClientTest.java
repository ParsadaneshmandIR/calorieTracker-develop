package com.parsa.calorietracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class OpenFoodFactsClientTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private OpenFoodFactsClient client;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        client = new OpenFoodFactsClient(
                RestClient.builder(),
                wm.getHttpBaseUrl(),
                "PersianCalorieTracker/1.0 (contact: test@example.com)");
    }

    @Test
    void maps_kcal_and_sets_odbl_license(WireMockRuntimeInfo wm) throws Exception {
        String body = """
                {
                  "status": 1,
                  "product": {
                    "product_name": "Test Yogurt",
                    "nutriments": {
                      "energy-kcal_100g": 60,
                      "proteins_100g": 3.5,
                      "carbohydrates_100g": 4.5,
                      "fat_100g": 3.0,
                      "fiber_100g": 0
                    }
                  }
                }
                """;
        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
                get(urlEqualTo("/api/v2/product/1234567890123.json"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)));

        Optional<Food> food = client.fetchByBarcode("1234567890123");

        assertThat(food).isPresent();
        assertThat(food.get().getSource()).isEqualTo(FoodSource.OPEN_FOOD_FACTS);
        assertThat(food.get().getState()).isEqualTo(FoodState.PACKAGED);
        assertThat(food.get().getKcalPer100g()).isEqualByComparingTo("60");
        assertThat(food.get().getLicense()).isEqualTo("ODbL-1.0");

        verify(getRequestedFor(urlEqualTo("/api/v2/product/1234567890123.json"))
                .withHeader("User-Agent", equalTo("PersianCalorieTracker/1.0 (contact: test@example.com)")));
    }

    @Test
    void falls_back_from_kj_to_kcal() throws Exception {
        JsonNode product = mapper.readTree("""
                {
                  "product_name": "KJ only",
                  "nutriments": {
                    "energy-kj_100g": 418.4,
                    "proteins_100g": 0,
                    "carbohydrates_100g": 25,
                    "fat_100g": 0
                  }
                }
                """);
        Optional<Food> food = client.mapProduct("999", product);
        assertThat(food).isPresent();
        assertThat(food.get().getKcalPer100g()).isEqualByComparingTo("100.0000");
    }
}

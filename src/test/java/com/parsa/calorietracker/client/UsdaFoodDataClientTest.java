package com.parsa.calorietracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class UsdaFoodDataClientTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private UsdaFoodDataClient client;

    @BeforeEach
    void setUp(WireMockRuntimeInfo wm) {
        client = new UsdaFoodDataClient(RestClient.builder(), wm.getHttpBaseUrl(), "test-key");
    }

    @Test
    void maps_nutrients_by_id_not_position(WireMockRuntimeInfo wm) throws Exception {
        String body = """
                {
                  "fdcId": 173944,
                  "description": "Apples, raw, with skin",
                  "foodNutrients": [
                    { "nutrient": { "id": 1003, "name": "Protein" }, "amount": 0.26 },
                    { "nutrient": { "id": 1008, "name": "Energy" }, "amount": 52 },
                    { "nutrient": { "id": 1005, "name": "Carbohydrate, by difference" }, "amount": 13.81 },
                    { "nutrient": { "id": 1004, "name": "Total lipid (fat)" }, "amount": 0.17 },
                    { "nutrient": { "id": 1079, "name": "Fiber, total dietary" }, "amount": 2.4 }
                  ]
                }
                """;
        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
                get(urlPathEqualTo("/food/173944"))
                        .willReturn(aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(body)));

        Optional<Food> food = client.fetchByFdcId(173944);

        assertThat(food).isPresent();
        assertThat(food.get().getSource()).isEqualTo(FoodSource.USDA);
        assertThat(food.get().getSourceId()).isEqualTo("173944");
        assertThat(food.get().getKcalPer100g()).isEqualByComparingTo("52");
        assertThat(food.get().getProteinPer100g()).isEqualByComparingTo("0.26");
        assertThat(food.get().getCarbPer100g()).isEqualByComparingTo("13.81");
        assertThat(food.get().getFatPer100g()).isEqualByComparingTo("0.17");
        assertThat(food.get().getFiberPer100g()).isEqualByComparingTo("2.4");
        assertThat(food.get().getLicense()).contains("USDA");

        verify(getRequestedFor(urlPathEqualTo("/food/173944")));
    }

    @Test
    void skips_inconsistent_macros() throws Exception {
        JsonNode node = mapper.readTree("""
                {
                  "fdcId": 1,
                  "description": "Broken",
                  "foodNutrients": [
                    { "nutrient": { "id": 1008, "name": "Energy" }, "amount": 900 },
                    { "nutrient": { "id": 1003, "name": "Protein" }, "amount": 1 },
                    { "nutrient": { "id": 1005, "name": "Carbohydrate" }, "amount": 1 },
                    { "nutrient": { "id": 1004, "name": "Total lipid (fat)" }, "amount": 1 }
                  ]
                }
                """);
        assertThat(client.mapFood(node)).isEmpty();
    }
}

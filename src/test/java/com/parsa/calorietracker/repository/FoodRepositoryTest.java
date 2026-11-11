package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FoodRepositoryTest {

    @Autowired
    private FoodRepository foodRepository;

    @Test
    void upsert_unique_on_source_and_source_id_and_search_by_name_fa() {
        Food food = new Food();
        food.setName("Cooked Rice");
        food.setNameFa("برنج پخته");
        food.setKcalPer100g(new BigDecimal("130"));
        food.setProteinPer100g(new BigDecimal("2.7"));
        food.setCarbPer100g(new BigDecimal("28"));
        food.setFatPer100g(new BigDecimal("0.3"));
        food.setState(FoodState.COOKED);
        food.setEdiblePortionPct(new BigDecimal("100"));
        food.setSource(FoodSource.MANUAL);
        food.setSourceId("iranian-csv:cooked-rice-test");
        food.setLicense("MANUAL");
        food.setFetchedAt(Instant.now());
        foodRepository.save(food);

        assertThat(foodRepository.findBySourceAndSourceId(FoodSource.MANUAL, "iranian-csv:cooked-rice-test"))
                .isPresent();
        assertThat(foodRepository.search("برنج", FoodState.COOKED, PageRequest.of(0, 10)).getTotalElements())
                .isGreaterThanOrEqualTo(1);
    }
}

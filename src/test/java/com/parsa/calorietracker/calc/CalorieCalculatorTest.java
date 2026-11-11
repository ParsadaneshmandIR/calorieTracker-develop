package com.parsa.calorietracker.calc;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalorieCalculatorTest {

    @Test
    void apple_100g_of_52_kcal_per_100g_yields_52() {
        Food apple = food("Apple", "52", "100");

        BigDecimal kcal = CalorieCalculator.kcal(apple, new BigDecimal("100"));

        assertThat(kcal).isEqualByComparingTo("52.0");
    }

    @Test
    void edible_portion_pct_is_applied_once() {
        // Whole egg shell ~13% waste → ediblePortionPct=87
        // 155 kcal/100g edible, 100 g as purchased → 155 * 100 * 87 / 10000 = 134.85 → 134.9
        Food egg = food("Egg", "155", "87");

        BigDecimal kcal = CalorieCalculator.kcal(egg, new BigDecimal("100"));

        assertThat(kcal).isEqualByComparingTo("134.9");

        // Same edible grams directly (87 g of edible egg) must match — proves no double-apply
        Food edibleOnly = food("Egg edible", "155", "100");
        BigDecimal fromEdibleGrams = CalorieCalculator.kcal(edibleOnly, new BigDecimal("87"));
        assertThat(kcal).isEqualByComparingTo(fromEdibleGrams);
    }

    @Test
    void negative_grams_throws() {
        Food apple = food("Apple", "52", "100");

        assertThatThrownBy(() -> CalorieCalculator.kcal(apple, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("grams");
    }

    @Test
    void zero_grams_throws() {
        Food apple = food("Apple", "52", "100");

        assertThatThrownBy(() -> CalorieCalculator.macros(apple, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void macrosConsistent_returns_false_for_broken_row() {
        Food broken = food("Broken", "500", "100");
        broken.setProteinPer100g(new BigDecimal("1"));
        broken.setCarbPer100g(new BigDecimal("1"));
        broken.setFatPer100g(new BigDecimal("1"));
        // Atwater: 4*1 + 4*1 + 9*1 = 17, far from 500

        assertThat(CalorieCalculator.macrosConsistent(broken)).isFalse();
    }

    @Test
    void macrosConsistent_returns_true_for_plausible_row() {
        Food apple = food("Apple", "52", "100");
        apple.setProteinPer100g(new BigDecimal("0.3"));
        apple.setCarbPer100g(new BigDecimal("14"));
        apple.setFatPer100g(new BigDecimal("0.2"));
        // 4*0.3 + 4*14 + 9*0.2 = 1.2 + 56 + 1.8 = 59 — within 20% of 52

        assertThat(CalorieCalculator.macrosConsistent(apple)).isTrue();
    }

    @Test
    void rounding_half_up_scale_1_and_sum_does_not_drift() {
        Food f = food("Item", "33.3", "100");
        f.setProteinPer100g(new BigDecimal("1"));
        f.setCarbPer100g(new BigDecimal("5"));
        f.setFatPer100g(new BigDecimal("1"));

        BigDecimal a = CalorieCalculator.kcal(f, new BigDecimal("10")); // 3.33 → 3.3
        BigDecimal b = CalorieCalculator.kcal(f, new BigDecimal("10"));
        BigDecimal c = CalorieCalculator.kcal(f, new BigDecimal("10"));

        assertThat(a).isEqualByComparingTo("3.3");
        BigDecimal sum = a.add(b).add(c);
        assertThat(sum).isEqualByComparingTo("9.9");

        // Direct 30 g calculation also scale-1 HALF_UP
        BigDecimal direct = CalorieCalculator.kcal(f, new BigDecimal("30"));
        assertThat(direct).isEqualByComparingTo("10.0");
        // Sum of rounded parts vs direct may differ by at most 0.1 — no unbounded drift
        assertThat(sum.subtract(direct).abs()).isLessThanOrEqualTo(new BigDecimal("0.1"));
    }

    @Test
    void macros_scales_all_nutrients() {
        Food f = food("Chicken", "165", "100");
        f.setProteinPer100g(new BigDecimal("31"));
        f.setCarbPer100g(new BigDecimal("0"));
        f.setFatPer100g(new BigDecimal("3.6"));

        MacroTotals totals = CalorieCalculator.macros(f, new BigDecimal("200"));

        assertThat(totals.kcal()).isEqualByComparingTo("330.0");
        assertThat(totals.protein()).isEqualByComparingTo("62.0");
        assertThat(totals.carb()).isEqualByComparingTo("0.0");
        assertThat(totals.fat().setScale(1, RoundingMode.HALF_UP)).isEqualByComparingTo("7.2");
    }

    private static Food food(String name, String kcal, String ediblePct) {
        Food f = new Food();
        f.setName(name);
        f.setNameFa(name);
        f.setKcalPer100g(new BigDecimal(kcal));
        f.setProteinPer100g(BigDecimal.ZERO);
        f.setCarbPer100g(BigDecimal.ZERO);
        f.setFatPer100g(BigDecimal.ZERO);
        f.setEdiblePortionPct(new BigDecimal(ediblePct));
        f.setState(FoodState.RAW);
        f.setSource(FoodSource.MANUAL);
        f.setSourceId("test-" + name);
        return f;
    }
}

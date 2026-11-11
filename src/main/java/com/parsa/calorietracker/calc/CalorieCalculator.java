package com.parsa.calorietracker.calc;

import com.parsa.calorietracker.domain.Food;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Pure calorie/macro math. Base unit is always kcal (or grams of macro) per 100 g
 * of the edible portion; ediblePortionPct scales as-purchased grams.
 */
public final class CalorieCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");
    private static final BigDecimal FOUR = new BigDecimal("4");
    private static final BigDecimal NINE = new BigDecimal("9");
    private static final BigDecimal TOLERANCE = new BigDecimal("0.20");
    private static final int SCALE = 1;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private CalorieCalculator() {
    }

    public static BigDecimal kcal(Food food, BigDecimal grams) {
        return scaleNutrient(requireFood(food).getKcalPer100g(), grams, food.getEdiblePortionPct());
    }

    public static MacroTotals macros(Food food, BigDecimal grams) {
        Food f = requireFood(food);
        BigDecimal edible = f.getEdiblePortionPct();
        return new MacroTotals(
                scaleNutrient(f.getKcalPer100g(), grams, edible),
                scaleNutrient(f.getProteinPer100g(), grams, edible),
                scaleNutrient(f.getCarbPer100g(), grams, edible),
                scaleNutrient(f.getFatPer100g(), grams, edible)
        );
    }

    /**
     * |4P + 4C + 9F − kcal| ≤ 20% of kcal. Used to reject bad import rows, not runtime.
     */
    public static boolean macrosConsistent(Food food) {
        Food f = requireFood(food);
        BigDecimal kcal = nullSafe(f.getKcalPer100g());
        if (kcal.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal atwater = atwater(f);
            return atwater.compareTo(BigDecimal.ZERO) == 0;
        }
        BigDecimal atwater = atwater(f);
        BigDecimal diff = atwater.subtract(kcal).abs();
        BigDecimal allowed = kcal.abs().multiply(TOLERANCE);
        return diff.compareTo(allowed) <= 0;
    }

    private static BigDecimal atwater(Food f) {
        return FOUR.multiply(nullSafe(f.getProteinPer100g()))
                .add(FOUR.multiply(nullSafe(f.getCarbPer100g())))
                .add(NINE.multiply(nullSafe(f.getFatPer100g())));
    }

    /**
     * valuePer100g * grams * ediblePortionPct / 10_000, HALF_UP, scale 1
     */
    private static BigDecimal scaleNutrient(BigDecimal per100g, BigDecimal grams, BigDecimal ediblePortionPct) {
        requirePositiveGrams(grams);
        BigDecimal edible = Objects.requireNonNullElse(ediblePortionPct, HUNDRED);
        if (edible.compareTo(BigDecimal.ZERO) <= 0 || edible.compareTo(HUNDRED) > 0) {
            throw new IllegalArgumentException("ediblePortionPct must be in (0, 100]: " + edible);
        }
        return nullSafe(per100g)
                .multiply(grams)
                .multiply(edible)
                .divide(TEN_THOUSAND, SCALE, ROUNDING);
    }

    private static void requirePositiveGrams(BigDecimal grams) {
        if (grams == null || grams.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("grams must be positive, got: " + grams);
        }
    }

    private static Food requireFood(Food food) {
        return Objects.requireNonNull(food, "food must not be null");
    }

    private static BigDecimal nullSafe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

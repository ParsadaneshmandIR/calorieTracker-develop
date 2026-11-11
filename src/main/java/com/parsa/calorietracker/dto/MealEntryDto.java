package com.parsa.calorietracker.dto;

import java.math.BigDecimal;

public record MealEntryDto(
        Long id,
        Long foodId,
        String foodNameFa,
        BigDecimal grams,
        Long portionId,
        BigDecimal computedKcal,
        BigDecimal computedProtein,
        BigDecimal computedCarb,
        BigDecimal computedFat
) {
}

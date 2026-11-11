package com.parsa.calorietracker.dto;

import java.math.BigDecimal;

public record CalcResponse(
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal carb,
        BigDecimal fat,
        BigDecimal grams,
        Breakdown breakdown
) {
    public record Breakdown(
            Long foodId,
            String nameFa,
            BigDecimal kcalPer100g,
            BigDecimal ediblePortionPct,
            BigDecimal gramsUsed
    ) {
    }
}

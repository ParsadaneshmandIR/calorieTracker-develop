package com.parsa.calorietracker.dto;

import java.math.BigDecimal;
import java.util.List;

public record RecipeNutritionDto(
        Long id,
        String name,
        String nameFa,
        Integer servings,
        BigDecimal totalWeightGrams,
        BigDecimal kcalPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbPer100g,
        BigDecimal fatPer100g,
        BigDecimal kcalPerServing,
        BigDecimal proteinPerServing,
        BigDecimal carbPerServing,
        BigDecimal fatPerServing,
        String instructions,
        List<IngredientLine> ingredients
) {
    public record IngredientLine(
            Long foodId,
            String nameFa,
            BigDecimal grams,
            BigDecimal kcal
    ) {
    }
}

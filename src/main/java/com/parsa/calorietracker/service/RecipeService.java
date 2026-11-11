package com.parsa.calorietracker.service;

import com.parsa.calorietracker.calc.CalorieCalculator;
import com.parsa.calorietracker.calc.MacroTotals;
import com.parsa.calorietracker.domain.Recipe;
import com.parsa.calorietracker.domain.RecipeIngredient;
import com.parsa.calorietracker.dto.RecipeNutritionDto;
import com.parsa.calorietracker.repository.RecipeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecipeService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Transactional(readOnly = true)
    public RecipeNutritionDto nutrition(Long id) {
        Recipe recipe = recipeRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recipe not found: " + id));

        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        List<RecipeNutritionDto.IngredientLine> lines = new ArrayList<>();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            MacroTotals macros = CalorieCalculator.macros(ingredient.getFood(), ingredient.getGrams());
            totalKcal = totalKcal.add(macros.kcal());
            totalProtein = totalProtein.add(macros.protein());
            totalCarb = totalCarb.add(macros.carb());
            totalFat = totalFat.add(macros.fat());
            lines.add(new RecipeNutritionDto.IngredientLine(
                    ingredient.getFood().getId(),
                    ingredient.getFood().getNameFa(),
                    ingredient.getGrams(),
                    macros.kcal()
            ));
        }

        BigDecimal weight = recipe.getTotalWeightGrams();
        BigDecimal servings = BigDecimal.valueOf(recipe.getServings());

        BigDecimal kcalPer100g = per100g(totalKcal, weight);
        BigDecimal proteinPer100g = per100g(totalProtein, weight);
        BigDecimal carbPer100g = per100g(totalCarb, weight);
        BigDecimal fatPer100g = per100g(totalFat, weight);

        return new RecipeNutritionDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getNameFa(),
                recipe.getServings(),
                weight,
                kcalPer100g,
                proteinPer100g,
                carbPer100g,
                fatPer100g,
                totalKcal.divide(servings, 1, RoundingMode.HALF_UP),
                totalProtein.divide(servings, 1, RoundingMode.HALF_UP),
                totalCarb.divide(servings, 1, RoundingMode.HALF_UP),
                totalFat.divide(servings, 1, RoundingMode.HALF_UP),
                recipe.getInstructions(),
                lines
        );
    }

    private static BigDecimal per100g(BigDecimal total, BigDecimal weightGrams) {
        return total.multiply(HUNDRED).divide(weightGrams, 1, RoundingMode.HALF_UP);
    }
}

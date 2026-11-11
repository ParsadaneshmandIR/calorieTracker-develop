package com.parsa.calorietracker.dto;

import com.parsa.calorietracker.domain.FoodState;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FoodCreateRequest(
        @NotBlank String name,
        @NotBlank String nameFa,
        @NotNull @DecimalMin("0") BigDecimal kcalPer100g,
        @NotNull @DecimalMin("0") BigDecimal proteinPer100g,
        @NotNull @DecimalMin("0") BigDecimal carbPer100g,
        @NotNull @DecimalMin("0") BigDecimal fatPer100g,
        @DecimalMin("0") BigDecimal fiberPer100g,
        @NotNull FoodState state,
        @NotNull @DecimalMin(value = "0.0001", inclusive = true) @DecimalMax("100") BigDecimal ediblePortionPct
) {
}

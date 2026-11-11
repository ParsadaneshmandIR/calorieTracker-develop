package com.parsa.calorietracker.dto;

import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;

import java.math.BigDecimal;
import java.util.List;

public record FoodDto(
        Long id,
        String name,
        String nameFa,
        BigDecimal kcalPer100g,
        BigDecimal proteinPer100g,
        BigDecimal carbPer100g,
        BigDecimal fatPer100g,
        BigDecimal fiberPer100g,
        FoodState state,
        BigDecimal ediblePortionPct,
        FoodSource source,
        String sourceId,
        String license,
        List<PortionDto> portions
) {
}

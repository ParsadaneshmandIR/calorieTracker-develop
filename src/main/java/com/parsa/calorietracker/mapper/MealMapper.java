package com.parsa.calorietracker.mapper;

import com.parsa.calorietracker.domain.Meal;
import com.parsa.calorietracker.domain.MealEntry;
import com.parsa.calorietracker.dto.MealDto;
import com.parsa.calorietracker.dto.MealEntryDto;

import java.math.BigDecimal;
import java.util.List;

public final class MealMapper {

    private MealMapper() {
    }

    public static MealDto toDto(Meal meal) {
        List<MealEntryDto> entries = meal.getEntries().stream().map(MealMapper::toEntryDto).toList();
        BigDecimal totalKcal = BigDecimal.ZERO;
        BigDecimal totalProtein = BigDecimal.ZERO;
        BigDecimal totalCarb = BigDecimal.ZERO;
        BigDecimal totalFat = BigDecimal.ZERO;
        for (MealEntryDto e : entries) {
            totalKcal = totalKcal.add(e.computedKcal());
            totalProtein = totalProtein.add(e.computedProtein());
            totalCarb = totalCarb.add(e.computedCarb());
            totalFat = totalFat.add(e.computedFat());
        }
        return new MealDto(
                meal.getId(),
                meal.getName(),
                meal.getEatenDate(),
                meal.getEatenTime(),
                meal.getCreatedAt(),
                entries,
                totalKcal,
                totalProtein,
                totalCarb,
                totalFat
        );
    }

    public static MealEntryDto toEntryDto(MealEntry entry) {
        return new MealEntryDto(
                entry.getId(),
                entry.getFood().getId(),
                entry.getFood().getNameFa(),
                entry.getGrams(),
                entry.getPortion() == null ? null : entry.getPortion().getId(),
                entry.getComputedKcal(),
                entry.getComputedProtein(),
                entry.getComputedCarb(),
                entry.getComputedFat()
        );
    }
}

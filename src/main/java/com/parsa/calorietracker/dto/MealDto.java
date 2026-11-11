package com.parsa.calorietracker.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MealDto(
        Long id,
        String name,
        LocalDate eatenDate,
        LocalTime eatenTime,
        Instant createdAt,
        List<MealEntryDto> entries,
        BigDecimal totalKcal,
        BigDecimal totalProtein,
        BigDecimal totalCarb,
        BigDecimal totalFat
) {
}

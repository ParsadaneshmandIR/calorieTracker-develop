package com.parsa.calorietracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record MealCreateRequest(
        @NotBlank String name,
        @NotNull LocalDate eatenDate,
        @NotNull LocalTime eatenTime
) {
}

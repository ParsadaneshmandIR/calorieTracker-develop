package com.parsa.calorietracker.dto;

import java.math.BigDecimal;

public record PortionDto(
        Long id,
        String label,
        String labelFa,
        BigDecimal grams
) {
}

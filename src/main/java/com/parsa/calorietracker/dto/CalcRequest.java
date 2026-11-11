package com.parsa.calorietracker.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CalcRequest(
        @NotNull Long foodId,
        @DecimalMin(value = "0.0001", inclusive = true) BigDecimal grams,
        Long portionId,
        @DecimalMin(value = "0.0001", inclusive = true) BigDecimal count
) {
}

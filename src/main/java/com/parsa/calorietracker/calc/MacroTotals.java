package com.parsa.calorietracker.calc;

import java.math.BigDecimal;

public record MacroTotals(
        BigDecimal kcal,
        BigDecimal protein,
        BigDecimal carb,
        BigDecimal fat
) {
}

package com.parsa.calorietracker.service;

import com.parsa.calorietracker.calc.CalorieCalculator;
import com.parsa.calorietracker.calc.MacroTotals;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.Portion;
import com.parsa.calorietracker.dto.CalcRequest;
import com.parsa.calorietracker.dto.CalcResponse;
import com.parsa.calorietracker.repository.PortionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

@Service
public class CalcService {

    private final FoodService foodService;
    private final PortionRepository portionRepository;

    public CalcService(FoodService foodService, PortionRepository portionRepository) {
        this.foodService = foodService;
        this.portionRepository = portionRepository;
    }

    @Transactional(readOnly = true)
    public CalcResponse calculate(CalcRequest request) {
        Food food = foodService.requireFood(request.foodId());
        BigDecimal grams = resolveGrams(request, food.getId());
        MacroTotals totals = CalorieCalculator.macros(food, grams);
        return new CalcResponse(
                totals.kcal(),
                totals.protein(),
                totals.carb(),
                totals.fat(),
                grams,
                new CalcResponse.Breakdown(
                        food.getId(),
                        food.getNameFa(),
                        food.getKcalPer100g(),
                        food.getEdiblePortionPct(),
                        grams
                )
        );
    }

    public BigDecimal resolveGrams(CalcRequest request, Long foodId) {
        if (request.portionId() != null) {
            if (request.count() == null) {
                throw new IllegalArgumentException("count is required when portionId is provided");
            }
            Portion portion = portionRepository.findById(request.portionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Portion not found: " + request.portionId()));
            if (!portion.getFood().getId().equals(foodId)) {
                throw new IllegalArgumentException("portionId does not belong to foodId");
            }
            return portion.getGrams().multiply(request.count());
        }
        if (request.grams() == null) {
            throw new IllegalArgumentException("Provide grams, or portionId with count");
        }
        if (request.grams().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("grams must be positive");
        }
        return request.grams();
    }
}

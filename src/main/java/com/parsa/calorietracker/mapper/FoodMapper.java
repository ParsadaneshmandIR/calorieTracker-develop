package com.parsa.calorietracker.mapper;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.Portion;
import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.dto.PortionDto;

import java.util.List;

public final class FoodMapper {

    private FoodMapper() {
    }

    public static FoodDto toDto(Food food, List<Portion> portions) {
        List<PortionDto> portionDtos = portions == null
                ? List.of()
                : portions.stream().map(FoodMapper::toPortionDto).toList();
        return new FoodDto(
                food.getId(),
                food.getName(),
                food.getNameFa(),
                food.getKcalPer100g(),
                food.getProteinPer100g(),
                food.getCarbPer100g(),
                food.getFatPer100g(),
                food.getFiberPer100g(),
                food.getState(),
                food.getEdiblePortionPct(),
                food.getSource(),
                food.getSourceId(),
                food.getLicense(),
                portionDtos
        );
    }

    public static FoodDto toSummaryDto(Food food) {
        return toDto(food, List.of());
    }

    public static PortionDto toPortionDto(Portion portion) {
        return new PortionDto(
                portion.getId(),
                portion.getLabel(),
                portion.getLabelFa(),
                portion.getGrams()
        );
    }
}

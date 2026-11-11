package com.parsa.calorietracker.web;

import com.parsa.calorietracker.dto.RecipeNutritionDto;
import com.parsa.calorietracker.service.RecipeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/{id}/nutrition")
    public RecipeNutritionDto nutrition(@PathVariable Long id) {
        return recipeService.nutrition(id);
    }
}

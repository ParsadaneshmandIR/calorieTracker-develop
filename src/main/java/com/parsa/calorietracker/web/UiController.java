package com.parsa.calorietracker.web;

import com.parsa.calorietracker.domain.FoodState;
import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.dto.MealDto;
import com.parsa.calorietracker.dto.PageResponse;
import com.parsa.calorietracker.dto.RecipeNutritionDto;
import com.parsa.calorietracker.service.FoodService;
import com.parsa.calorietracker.service.MealService;
import com.parsa.calorietracker.service.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
public class UiController {

    private final FoodService foodService;
    private final MealService mealService;
    private final RecipeService recipeService;

    public UiController(FoodService foodService, MealService mealService, RecipeService recipeService) {
        this.foodService = foodService;
        this.mealService = mealService;
        this.recipeService = recipeService;
    }

    @GetMapping("/")
    public String search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) FoodState state,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        PageResponse<FoodDto> foods = foodService.search(q, state, page, 20);
        model.addAttribute("foods", foods);
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("state", state);
        model.addAttribute("pageTitle", "جست‌وجو");
        return "search";
    }

    @GetMapping("/calculator")
    public String calculator(@RequestParam(required = false) Long foodId, Model model) {
        model.addAttribute("pageTitle", "ماشین‌حساب");
        if (foodId != null) {
            model.addAttribute("food", foodService.getById(foodId));
        }
        PageResponse<FoodDto> foods = foodService.search(null, null, 0, 50);
        model.addAttribute("foodOptions", foods.content());
        return "calculator";
    }

    @GetMapping("/meals")
    public String meals(Model model) {
        LocalDate today = LocalDate.now();
        List<MealDto> meals = mealService.listBetween(today, today);
        BigDecimal dayKcal = BigDecimal.ZERO;
        BigDecimal dayProtein = BigDecimal.ZERO;
        BigDecimal dayCarb = BigDecimal.ZERO;
        BigDecimal dayFat = BigDecimal.ZERO;
        for (MealDto m : meals) {
            dayKcal = dayKcal.add(m.totalKcal());
            dayProtein = dayProtein.add(m.totalProtein());
            dayCarb = dayCarb.add(m.totalCarb());
            dayFat = dayFat.add(m.totalFat());
        }
        model.addAttribute("meals", meals);
        model.addAttribute("dayKcal", dayKcal);
        model.addAttribute("dayProtein", dayProtein);
        model.addAttribute("dayCarb", dayCarb);
        model.addAttribute("dayFat", dayFat);
        model.addAttribute("today", today);
        model.addAttribute("foodOptions", foodService.search(null, null, 0, 50).content());
        model.addAttribute("pageTitle", "وعده‌های من");
        return "meals";
    }

    @GetMapping("/recipes/{id}")
    public String recipe(@PathVariable Long id, Model model) {
        try {
            RecipeNutritionDto nutrition = recipeService.nutrition(id);
            model.addAttribute("recipe", nutrition);
            model.addAttribute("pageTitle", nutrition.nameFa());
            return "recipe";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", "دستور پخت پیدا نشد.");
            model.addAttribute("pageTitle", "دستور پخت");
            return "recipe";
        }
    }
}

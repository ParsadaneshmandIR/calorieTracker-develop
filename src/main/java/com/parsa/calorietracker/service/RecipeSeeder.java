package com.parsa.calorietracker.service;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.Recipe;
import com.parsa.calorietracker.domain.RecipeIngredient;
import com.parsa.calorietracker.repository.FoodRepository;
import com.parsa.calorietracker.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@Order(3)
public class RecipeSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(RecipeSeeder.class);

    private final RecipeRepository recipeRepository;
    private final FoodRepository foodRepository;

    public RecipeSeeder(RecipeRepository recipeRepository, FoodRepository foodRepository) {
        this.recipeRepository = recipeRepository;
        this.foodRepository = foodRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (recipeRepository.count() > 0) {
            return;
        }
        Optional<Food> rice = foodRepository.findBySourceAndSourceId(FoodSource.MANUAL, "iranian-csv:cooked-rice");
        Optional<Food> chicken = foodRepository.findBySourceAndSourceId(FoodSource.MANUAL, "iranian-csv:joojeh-kabab");
        Optional<Food> yogurt = foodRepository.findBySourceAndSourceId(FoodSource.MANUAL, "iranian-csv:yogurt");
        if (rice.isEmpty() || chicken.isEmpty() || yogurt.isEmpty()) {
            log.warn("Skipping sample recipe seed; required foods missing");
            return;
        }

        Recipe recipe = new Recipe();
        recipe.setName("Zereshk Polo with Chicken (sample)");
        recipe.setNameFa("زرشک‌پلو با مرغ (نمونه)");
        recipe.setTotalWeightGrams(new BigDecimal("900"));
        recipe.setServings(4);
        recipe.setInstructions("برنج پخته، جوجه کباب و کمی ماست را ترکیب کنید. مقادیر تقریبی برای نمایش تغذیهٔ ترکیبی است.");

        add(recipe, rice.get(), new BigDecimal("500"));
        add(recipe, chicken.get(), new BigDecimal("350"));
        add(recipe, yogurt.get(), new BigDecimal("50"));

        recipeRepository.save(recipe);
        log.info("Seeded sample recipe id={}", recipe.getId());
    }

    private static void add(Recipe recipe, Food food, BigDecimal grams) {
        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setRecipe(recipe);
        ingredient.setFood(food);
        ingredient.setGrams(grams);
        recipe.getIngredients().add(ingredient);
    }
}

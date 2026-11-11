package com.parsa.calorietracker.service;

import com.parsa.calorietracker.calc.CalorieCalculator;
import com.parsa.calorietracker.calc.MacroTotals;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.Meal;
import com.parsa.calorietracker.domain.MealEntry;
import com.parsa.calorietracker.domain.Portion;
import com.parsa.calorietracker.dto.CalcRequest;
import com.parsa.calorietracker.dto.MealCreateRequest;
import com.parsa.calorietracker.dto.MealDto;
import com.parsa.calorietracker.dto.MealEntryCreateRequest;
import com.parsa.calorietracker.dto.MealEntryDto;
import com.parsa.calorietracker.mapper.MealMapper;
import com.parsa.calorietracker.repository.MealEntryRepository;
import com.parsa.calorietracker.repository.MealRepository;
import com.parsa.calorietracker.repository.PortionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class MealService {

    private final MealRepository mealRepository;
    private final MealEntryRepository mealEntryRepository;
    private final FoodService foodService;
    private final CalcService calcService;
    private final PortionRepository portionRepository;

    public MealService(MealRepository mealRepository,
                       MealEntryRepository mealEntryRepository,
                       FoodService foodService,
                       CalcService calcService,
                       PortionRepository portionRepository) {
        this.mealRepository = mealRepository;
        this.mealEntryRepository = mealEntryRepository;
        this.foodService = foodService;
        this.calcService = calcService;
        this.portionRepository = portionRepository;
    }

    @Transactional
    public MealDto create(MealCreateRequest request) {
        Meal meal = new Meal();
        meal.setName(request.name().trim());
        meal.setEatenDate(request.eatenDate());
        meal.setEatenTime(request.eatenTime());
        meal.setCreatedAt(Instant.now());
        return MealMapper.toDto(mealRepository.save(meal));
    }

    @Transactional
    public MealEntryDto addEntry(Long mealId, MealEntryCreateRequest request) {
        Meal meal = mealRepository.findById(mealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal not found: " + mealId));
        Food food = foodService.requireFood(request.foodId());

        CalcRequest calcRequest = new CalcRequest(request.foodId(), request.grams(), request.portionId(), request.count());
        BigDecimal grams = calcService.resolveGrams(calcRequest, food.getId());
        MacroTotals totals = CalorieCalculator.macros(food, grams);

        MealEntry entry = new MealEntry();
        entry.setMeal(meal);
        entry.setFood(food);
        entry.setGrams(grams);
        if (request.portionId() != null) {
            Portion portion = portionRepository.findById(request.portionId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Portion not found"));
            entry.setPortion(portion);
        }
        // Snapshot macros at insert time
        entry.setComputedKcal(totals.kcal());
        entry.setComputedProtein(totals.protein());
        entry.setComputedCarb(totals.carb());
        entry.setComputedFat(totals.fat());

        meal.addEntry(entry);
        entry = mealEntryRepository.save(entry);
        return MealMapper.toEntryDto(entry);
    }

    @Transactional(readOnly = true)
    public List<MealDto> listBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("from and to dates are required");
        }
        if (to.isBefore(from)) {
            throw new IllegalArgumentException("to must be on or after from");
        }
        return mealRepository.findBetween(from, to).stream().map(MealMapper::toDto).toList();
    }

    @Transactional
    public void deleteEntry(Long mealId, Long entryId) {
        MealEntry entry = mealEntryRepository.findByIdAndMealId(entryId, mealId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal entry not found"));
        mealEntryRepository.delete(entry);
    }
}

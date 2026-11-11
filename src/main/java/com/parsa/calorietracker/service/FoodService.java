package com.parsa.calorietracker.service;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import com.parsa.calorietracker.domain.Portion;
import com.parsa.calorietracker.dto.FoodCreateRequest;
import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.dto.PageResponse;
import com.parsa.calorietracker.mapper.FoodMapper;
import com.parsa.calorietracker.repository.FoodRepository;
import com.parsa.calorietracker.repository.PortionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class FoodService {

    private final FoodRepository foodRepository;
    private final PortionRepository portionRepository;

    public FoodService(FoodRepository foodRepository, PortionRepository portionRepository) {
        this.foodRepository = foodRepository;
        this.portionRepository = portionRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foodSearch", key = "#q + '|' + #state + '|' + #page + '|' + #size")
    public PageResponse<FoodDto> search(String q, FoodState state, int page, int size) {
        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("nameFa"));
        Page<Food> result = foodRepository.search(blankToNull(q), state, pageable);
        return PageResponse.from(result.map(FoodMapper::toSummaryDto));
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foodById", key = "#id")
    public FoodDto getById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found: " + id));
        List<Portion> portions = portionRepository.findByFoodIdOrderByGramsAsc(id);
        return FoodMapper.toDto(food, portions);
    }

    @Transactional(readOnly = true)
    public Food requireFood(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food not found: " + id));
    }

    @Transactional
    public FoodDto createManual(FoodCreateRequest request) {
        validateNutrients(request.kcalPer100g(), request.proteinPer100g(), request.carbPer100g(),
                request.fatPer100g(), request.ediblePortionPct());

        Food food = new Food();
        food.setName(request.name().trim());
        food.setNameFa(request.nameFa().trim());
        food.setKcalPer100g(request.kcalPer100g());
        food.setProteinPer100g(request.proteinPer100g());
        food.setCarbPer100g(request.carbPer100g());
        food.setFatPer100g(request.fatPer100g());
        food.setFiberPer100g(request.fiberPer100g());
        food.setState(request.state());
        food.setEdiblePortionPct(request.ediblePortionPct());
        food.setSource(FoodSource.MANUAL);
        food.setSourceId("manual:" + UUID.randomUUID());
        food.setLicense("MANUAL");
        food.setFetchedAt(Instant.now());

        food = foodRepository.save(food);
        return FoodMapper.toDto(food, List.of());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "foodByBarcode", key = "#barcode")
    public FoodDto getByBarcodeCached(String barcode) {
        return foodRepository.findBySourceAndSourceId(FoodSource.OPEN_FOOD_FACTS, barcode)
                .map(f -> FoodMapper.toDto(f, portionRepository.findByFoodIdOrderByGramsAsc(f.getId())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No cached product for barcode: " + barcode));
    }

    public static void validateNutrients(BigDecimal kcal, BigDecimal protein, BigDecimal carb,
                                         BigDecimal fat, BigDecimal ediblePortionPct) {
        requireNonNegative("kcalPer100g", kcal);
        requireNonNegative("proteinPer100g", protein);
        requireNonNegative("carbPer100g", carb);
        requireNonNegative("fatPer100g", fat);
        if (ediblePortionPct == null
                || ediblePortionPct.compareTo(BigDecimal.ZERO) <= 0
                || ediblePortionPct.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("ediblePortionPct must be in (0, 100]");
        }
    }

    private static void requireNonNegative(String field, BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " must be non-negative");
        }
    }

    private static String blankToNull(String q) {
        return q == null || q.isBlank() ? null : q.trim();
    }
}

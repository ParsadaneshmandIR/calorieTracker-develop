package com.parsa.calorietracker.service;

import com.parsa.calorietracker.client.OpenFoodFactsClient;
import com.parsa.calorietracker.client.UsdaFoodDataClient;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.mapper.FoodMapper;
import com.parsa.calorietracker.repository.FoodRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Offline import/refresh only. Controllers that serve users must never call third-party APIs
 * synchronously for nutrition — they read from the local DB/cache.
 */
@Service
public class ImportService {

    private static final Logger log = LoggerFactory.getLogger(ImportService.class);

    private final UsdaFoodDataClient usdaFoodDataClient;
    private final OpenFoodFactsClient openFoodFactsClient;
    private final FoodRepository foodRepository;

    public ImportService(UsdaFoodDataClient usdaFoodDataClient,
                         OpenFoodFactsClient openFoodFactsClient,
                         FoodRepository foodRepository) {
        this.usdaFoodDataClient = usdaFoodDataClient;
        this.openFoodFactsClient = openFoodFactsClient;
        this.foodRepository = foodRepository;
    }

    @Transactional
    public List<FoodDto> importUsda(String query) {
        List<Food> mapped = usdaFoodDataClient.searchAndFetch(query);
        List<FoodDto> saved = new ArrayList<>();
        for (Food incoming : mapped) {
            Food food = upsert(incoming);
            log.info("Imported food source={} sourceId={}", food.getSource(), food.getSourceId());
            saved.add(FoodMapper.toSummaryDto(food));
        }
        return saved;
    }

    /**
     * Admin/offline refresh for a barcode. User-facing barcode GET only reads the local cache.
     */
    @Transactional
    public FoodDto importOpenFoodFactsBarcode(String barcode) {
        Food incoming = openFoodFactsClient.fetchByBarcode(barcode)
                .orElseThrow(() -> new IllegalArgumentException("Product not found on Open Food Facts: " + barcode));
        Food food = upsert(incoming);
        log.info("Imported food source={} sourceId={}", food.getSource(), food.getSourceId());
        return FoodMapper.toSummaryDto(food);
    }

    private Food upsert(Food incoming) {
        Food food = foodRepository.findBySourceAndSourceId(incoming.getSource(), incoming.getSourceId())
                .orElseGet(Food::new);
        food.setName(incoming.getName());
        food.setNameFa(incoming.getNameFa());
        food.setKcalPer100g(incoming.getKcalPer100g());
        food.setProteinPer100g(incoming.getProteinPer100g());
        food.setCarbPer100g(incoming.getCarbPer100g());
        food.setFatPer100g(incoming.getFatPer100g());
        food.setFiberPer100g(incoming.getFiberPer100g());
        food.setState(incoming.getState());
        food.setEdiblePortionPct(incoming.getEdiblePortionPct());
        food.setSource(incoming.getSource());
        food.setSourceId(incoming.getSourceId());
        food.setLicense(incoming.getLicense());
        food.setFetchedAt(incoming.getFetchedAt());
        return foodRepository.save(food);
    }
}

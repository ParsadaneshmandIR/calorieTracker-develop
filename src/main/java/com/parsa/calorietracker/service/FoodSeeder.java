package com.parsa.calorietracker.service;

import com.parsa.calorietracker.csv.IranianFoodCsvLoader;
import com.parsa.calorietracker.csv.IranianFoodCsvLoader.CsvFoodRow;
import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.Portion;
import com.parsa.calorietracker.repository.FoodRepository;
import com.parsa.calorietracker.repository.PortionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
public class FoodSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(FoodSeeder.class);

    private static final List<PortionDef> COMMON_PORTIONS = List.of(
            new PortionDef("tablespoon", "قاشق غذاخوری", new BigDecimal("15")),
            new PortionDef("teaspoon", "قاشق چای‌خوری", new BigDecimal("5")),
            new PortionDef("glass", "لیوان", new BigDecimal("240")),
            new PortionDef("ladle", "کفگیر", new BigDecimal("120")),
            new PortionDef("slice", "برش", new BigDecimal("30"))
    );

    private final FoodRepository foodRepository;
    private final PortionRepository portionRepository;

    public FoodSeeder(FoodRepository foodRepository, PortionRepository portionRepository) {
        this.foodRepository = foodRepository;
        this.portionRepository = portionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource resource = new ClassPathResource("data/iranian-foods.csv");
        try (InputStream in = resource.getInputStream()) {
            List<CsvFoodRow> rows = IranianFoodCsvLoader.load(in);
            int upserted = 0;
            for (CsvFoodRow row : rows) {
                upsert(row);
                upserted++;
            }
            log.info("Seeded/upserted {} Iranian foods from CSV (source=MANUAL)", upserted);
        }
    }

    private void upsert(CsvFoodRow row) {
        Food incoming = row.food();
        Food food = foodRepository.findBySourceAndSourceId(FoodSource.MANUAL, incoming.getSourceId())
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
        food.setSource(FoodSource.MANUAL);
        food.setSourceId(incoming.getSourceId());
        food.setLicense(incoming.getLicense());
        food.setFetchedAt(incoming.getFetchedAt());

        food = foodRepository.save(food);
        log.debug("Upserted food source={} sourceId={}", food.getSource(), food.getSourceId());

        ensurePortions(food, row.defaultPortionLabelFa(), row.defaultPortionGrams());
    }

    private void ensurePortions(Food food, String defaultLabelFa, BigDecimal defaultGrams) {
        for (PortionDef def : COMMON_PORTIONS) {
            if (!portionRepository.existsByFoodIdAndLabel(food.getId(), def.label())) {
                Portion p = new Portion();
                p.setFood(food);
                p.setLabel(def.label());
                p.setLabelFa(def.labelFa());
                p.setGrams(def.grams());
                portionRepository.save(p);
            }
        }
        // Ensure the CSV default portion exists with its specific grams (may override common if same labelFa)
        String mappedLabel = mapLabelFaToEn(defaultLabelFa);
        if (!portionRepository.existsByFoodIdAndLabel(food.getId(), mappedLabel + "-default")) {
            Portion custom = new Portion();
            custom.setFood(food);
            custom.setLabel(mappedLabel + "-default");
            custom.setLabelFa(defaultLabelFa + " (پیش‌فرض)");
            custom.setGrams(defaultGrams);
            portionRepository.save(custom);
        }
    }

    private static String mapLabelFaToEn(String labelFa) {
        return switch (labelFa) {
            case "قاشق غذاخوری" -> "tablespoon";
            case "قاشق چای‌خوری" -> "teaspoon";
            case "لیوان" -> "glass";
            case "کفگیر" -> "ladle";
            case "برش" -> "slice";
            default -> "custom";
        };
    }

    private record PortionDef(String label, String labelFa, BigDecimal grams) {
    }
}

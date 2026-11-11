package com.parsa.calorietracker.csv;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class IranianFoodCsvLoader {

    public record CsvFoodRow(
            Food food,
            String defaultPortionLabelFa,
            BigDecimal defaultPortionGrams,
            String notes
    ) {
    }

    private IranianFoodCsvLoader() {
    }

    public static List<CsvFoodRow> load(InputStream inputStream) throws IOException {
        List<CsvFoodRow> rows = new ArrayList<>();
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) {
                return rows;
            }
            String line;
            int lineNo = 1;
            while ((line = reader.readLine()) != null) {
                lineNo++;
                if (line.isBlank()) {
                    continue;
                }
                rows.add(parseLine(line, lineNo));
            }
        }
        return rows;
    }

    private static CsvFoodRow parseLine(String line, int lineNo) {
        String[] cols = splitCsv(line);
        if (cols.length < 10) {
            throw new IllegalArgumentException("CSV line " + lineNo + " has fewer than 10 columns: " + line);
        }
        String nameFa = cols[0].trim();
        String name = cols[1].trim();
        BigDecimal kcal = decimal(cols[2], lineNo, "kcalPer100g");
        BigDecimal protein = decimal(cols[3], lineNo, "protein");
        BigDecimal carb = decimal(cols[4], lineNo, "carb");
        BigDecimal fat = decimal(cols[5], lineNo, "fat");
        FoodState state = FoodState.valueOf(cols[6].trim().toUpperCase(Locale.ROOT));
        String portionLabelFa = cols[7].trim();
        BigDecimal portionGrams = decimal(cols[8], lineNo, "defaultPortionGrams");
        String notes = cols[9].trim();

        if (kcal.compareTo(BigDecimal.ZERO) < 0 || protein.compareTo(BigDecimal.ZERO) < 0
                || carb.compareTo(BigDecimal.ZERO) < 0 || fat.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negative nutrient on CSV line " + lineNo);
        }
        if (portionGrams.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Portion grams must be positive on CSV line " + lineNo);
        }

        Food food = new Food();
        food.setName(name);
        food.setNameFa(nameFa);
        food.setKcalPer100g(kcal);
        food.setProteinPer100g(protein);
        food.setCarbPer100g(carb);
        food.setFatPer100g(fat);
        food.setState(state);
        food.setEdiblePortionPct(new BigDecimal("100"));
        food.setSource(FoodSource.MANUAL);
        food.setSourceId("iranian-csv:" + slug(name));
        food.setLicense("MANUAL-ESTIMATE");
        food.setFetchedAt(Instant.now());

        return new CsvFoodRow(food, portionLabelFa, portionGrams, notes);
    }

    static String[] splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(String[]::new);
    }

    private static BigDecimal decimal(String raw, int lineNo, String field) {
        try {
            return new BigDecimal(raw.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + field + " on CSV line " + lineNo + ": " + raw, ex);
        }
    }

    private static String slug(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}

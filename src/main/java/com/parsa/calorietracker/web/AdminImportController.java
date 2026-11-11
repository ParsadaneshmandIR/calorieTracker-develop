package com.parsa.calorietracker.web;

import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.service.ImportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/import")
public class AdminImportController {

    private final ImportService importService;

    public AdminImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping("/usda")
    public Map<String, Object> importUsda(@RequestParam String query) {
        List<FoodDto> foods = importService.importUsda(query);
        return Map.of("imported", foods.size(), "foods", foods);
    }

    @PostMapping("/off")
    public FoodDto importOff(@RequestParam String barcode) {
        return importService.importOpenFoodFactsBarcode(barcode);
    }
}

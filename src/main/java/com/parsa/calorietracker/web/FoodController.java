package com.parsa.calorietracker.web;

import com.parsa.calorietracker.domain.FoodState;
import com.parsa.calorietracker.dto.FoodCreateRequest;
import com.parsa.calorietracker.dto.FoodDto;
import com.parsa.calorietracker.dto.PageResponse;
import com.parsa.calorietracker.service.FoodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @GetMapping
    public PageResponse<FoodDto> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) FoodState state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return foodService.search(q, state, page, size);
    }

    @GetMapping("/{id}")
    public FoodDto get(@PathVariable Long id) {
        return foodService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodDto create(@Valid @RequestBody FoodCreateRequest request) {
        return foodService.createManual(request);
    }

    /**
     * Returns a locally cached OFF product only — never calls the network.
     */
    @GetMapping("/barcode/{barcode}")
    public FoodDto byBarcode(@PathVariable String barcode) {
        return foodService.getByBarcodeCached(barcode);
    }
}

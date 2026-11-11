package com.parsa.calorietracker.web;

import com.parsa.calorietracker.dto.MealCreateRequest;
import com.parsa.calorietracker.dto.MealDto;
import com.parsa.calorietracker.dto.MealEntryCreateRequest;
import com.parsa.calorietracker.dto.MealEntryDto;
import com.parsa.calorietracker.service.MealService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/meals")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MealDto create(@Valid @RequestBody MealCreateRequest request) {
        return mealService.create(request);
    }

    @PostMapping("/{id}/entries")
    @ResponseStatus(HttpStatus.CREATED)
    public MealEntryDto addEntry(@PathVariable Long id, @Valid @RequestBody MealEntryCreateRequest request) {
        return mealService.addEntry(id, request);
    }

    @GetMapping
    public List<MealDto> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return mealService.listBetween(from, to);
    }

    @DeleteMapping("/{id}/entries/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long id, @PathVariable Long entryId) {
        mealService.deleteEntry(id, entryId);
    }
}

package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.MealEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    Optional<MealEntry> findByIdAndMealId(Long id, Long mealId);
}

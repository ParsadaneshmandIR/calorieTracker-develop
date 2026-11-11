package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("""
            SELECT r FROM Recipe r
            LEFT JOIN FETCH r.ingredients i
            LEFT JOIN FETCH i.food
            WHERE r.id = :id
            """)
    Optional<Recipe> findByIdWithIngredients(@Param("id") Long id);
}

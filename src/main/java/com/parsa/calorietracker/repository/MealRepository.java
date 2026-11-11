package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.Meal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MealRepository extends JpaRepository<Meal, Long> {

    @Query("""
            SELECT DISTINCT m FROM Meal m
            LEFT JOIN FETCH m.entries e
            LEFT JOIN FETCH e.food
            WHERE m.eatenDate >= :from AND m.eatenDate <= :to
            ORDER BY m.eatenDate DESC, m.eatenTime DESC
            """)
    List<Meal> findBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            SELECT m FROM Meal m
            LEFT JOIN FETCH m.entries e
            LEFT JOIN FETCH e.food
            WHERE m.id = :id
            """)
    Optional<Meal> findByIdWithEntries(@Param("id") Long id);
}

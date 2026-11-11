package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.Food;
import com.parsa.calorietracker.domain.FoodSource;
import com.parsa.calorietracker.domain.FoodState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {

    Optional<Food> findBySourceAndSourceId(FoodSource source, String sourceId);

    @Query("""
            SELECT f FROM Food f
            WHERE (:q IS NULL OR :q = '' OR LOWER(f.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR f.nameFa LIKE CONCAT('%', :q, '%'))
              AND (:state IS NULL OR f.state = :state)
            """)
    Page<Food> search(@Param("q") String q, @Param("state") FoodState state, Pageable pageable);
}

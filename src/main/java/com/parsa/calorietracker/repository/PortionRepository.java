package com.parsa.calorietracker.repository;

import com.parsa.calorietracker.domain.Portion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortionRepository extends JpaRepository<Portion, Long> {

    List<Portion> findByFoodIdOrderByGramsAsc(Long foodId);

    boolean existsByFoodIdAndLabel(Long foodId, String label);
}

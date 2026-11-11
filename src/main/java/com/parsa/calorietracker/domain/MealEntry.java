package com.parsa.calorietracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "meal_entry")
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal grams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portion_id")
    private Portion portion;

    @Column(name = "computed_kcal", nullable = false, precision = 12, scale = 4)
    private BigDecimal computedKcal;

    @Column(name = "computed_protein", nullable = false, precision = 12, scale = 4)
    private BigDecimal computedProtein;

    @Column(name = "computed_carb", nullable = false, precision = 12, scale = 4)
    private BigDecimal computedCarb;

    @Column(name = "computed_fat", nullable = false, precision = 12, scale = 4)
    private BigDecimal computedFat;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Meal getMeal() {
        return meal;
    }

    public void setMeal(Meal meal) {
        this.meal = meal;
    }

    public Food getFood() {
        return food;
    }

    public void setFood(Food food) {
        this.food = food;
    }

    public BigDecimal getGrams() {
        return grams;
    }

    public void setGrams(BigDecimal grams) {
        this.grams = grams;
    }

    public Portion getPortion() {
        return portion;
    }

    public void setPortion(Portion portion) {
        this.portion = portion;
    }

    public BigDecimal getComputedKcal() {
        return computedKcal;
    }

    public void setComputedKcal(BigDecimal computedKcal) {
        this.computedKcal = computedKcal;
    }

    public BigDecimal getComputedProtein() {
        return computedProtein;
    }

    public void setComputedProtein(BigDecimal computedProtein) {
        this.computedProtein = computedProtein;
    }

    public BigDecimal getComputedCarb() {
        return computedCarb;
    }

    public void setComputedCarb(BigDecimal computedCarb) {
        this.computedCarb = computedCarb;
    }

    public BigDecimal getComputedFat() {
        return computedFat;
    }

    public void setComputedFat(BigDecimal computedFat) {
        this.computedFat = computedFat;
    }
}

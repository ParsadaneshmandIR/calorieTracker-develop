package com.parsa.calorietracker.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal")
public class Meal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "eaten_date", nullable = false)
    private LocalDate eatenDate;

    @Column(name = "eaten_time", nullable = false)
    private LocalTime eatenTime;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "meal", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealEntry> entries = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getEatenDate() {
        return eatenDate;
    }

    public void setEatenDate(LocalDate eatenDate) {
        this.eatenDate = eatenDate;
    }

    public LocalTime getEatenTime() {
        return eatenTime;
    }

    public void setEatenTime(LocalTime eatenTime) {
        this.eatenTime = eatenTime;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<MealEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MealEntry> entries) {
        this.entries = entries;
    }

    public void addEntry(MealEntry entry) {
        entries.add(entry);
        entry.setMeal(this);
    }

    public void removeEntry(MealEntry entry) {
        entries.remove(entry);
        entry.setMeal(null);
    }
}

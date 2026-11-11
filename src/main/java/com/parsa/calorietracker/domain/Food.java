package com.parsa.calorietracker.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "food", uniqueConstraints = @UniqueConstraint(name = "uq_food_source_source_id", columnNames = {"source", "source_id"}))
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "name_fa", nullable = false)
    private String nameFa;

    @Column(name = "kcal_per_100g", nullable = false, precision = 12, scale = 4)
    private BigDecimal kcalPer100g;

    @Column(name = "protein_per_100g", nullable = false, precision = 12, scale = 4)
    private BigDecimal proteinPer100g;

    @Column(name = "carb_per_100g", nullable = false, precision = 12, scale = 4)
    private BigDecimal carbPer100g;

    @Column(name = "fat_per_100g", nullable = false, precision = 12, scale = 4)
    private BigDecimal fatPer100g;

    @Column(name = "fiber_per_100g", precision = 12, scale = 4)
    private BigDecimal fiberPer100g;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FoodState state;

    @Column(name = "edible_portion_pct", nullable = false, precision = 8, scale = 4)
    private BigDecimal ediblePortionPct = new BigDecimal("100");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private FoodSource source;

    @Column(name = "source_id", nullable = false, length = 128)
    private String sourceId;

    @Column(length = 128)
    private String license;

    @Column(name = "fetched_at")
    private Instant fetchedAt;

    @Version
    private Long version;

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

    public String getNameFa() {
        return nameFa;
    }

    public void setNameFa(String nameFa) {
        this.nameFa = nameFa;
    }

    public BigDecimal getKcalPer100g() {
        return kcalPer100g;
    }

    public void setKcalPer100g(BigDecimal kcalPer100g) {
        this.kcalPer100g = kcalPer100g;
    }

    public BigDecimal getProteinPer100g() {
        return proteinPer100g;
    }

    public void setProteinPer100g(BigDecimal proteinPer100g) {
        this.proteinPer100g = proteinPer100g;
    }

    public BigDecimal getCarbPer100g() {
        return carbPer100g;
    }

    public void setCarbPer100g(BigDecimal carbPer100g) {
        this.carbPer100g = carbPer100g;
    }

    public BigDecimal getFatPer100g() {
        return fatPer100g;
    }

    public void setFatPer100g(BigDecimal fatPer100g) {
        this.fatPer100g = fatPer100g;
    }

    public BigDecimal getFiberPer100g() {
        return fiberPer100g;
    }

    public void setFiberPer100g(BigDecimal fiberPer100g) {
        this.fiberPer100g = fiberPer100g;
    }

    public FoodState getState() {
        return state;
    }

    public void setState(FoodState state) {
        this.state = state;
    }

    public BigDecimal getEdiblePortionPct() {
        return ediblePortionPct;
    }

    public void setEdiblePortionPct(BigDecimal ediblePortionPct) {
        this.ediblePortionPct = ediblePortionPct;
    }

    public FoodSource getSource() {
        return source;
    }

    public void setSource(FoodSource source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}

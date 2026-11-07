package com.electricity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "appliances")
public class Appliance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String category = "General"; 

    @Column(nullable = false)
    private Integer wattage;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Double dailyHours = 2.0;

    @Column(nullable = false)
    private Double monthlyKwh;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Appliance() {}

    public Appliance(Household household, String name, String category, Integer wattage, Integer quantity, Double dailyHours) {
        this.household = household;
        this.name = name;
        this.category = category;
        this.wattage = wattage;
        this.quantity = quantity;
        this.dailyHours = dailyHours;
        this.calculateMonthlyKwh();
        this.createdAt = LocalDateTime.now();
    }

    public void calculateMonthlyKwh() {
        if (wattage != null && quantity != null && dailyHours != null) {
            this.monthlyKwh = Math.round(((wattage * quantity * dailyHours * 30.0) / 1000.0) * 10.0) / 10.0;
        } else {
            this.monthlyKwh = 0.0;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getWattage() { return wattage; }
    public void setWattage(Integer wattage) { this.wattage = wattage; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getDailyHours() { return dailyHours; }
    public void setDailyHours(Double dailyHours) { this.dailyHours = dailyHours; }

    public Double getMonthlyKwh() { return monthlyKwh; }
    public void setMonthlyKwh(Double monthlyKwh) { this.monthlyKwh = monthlyKwh; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

package com.electricity.dto;

public class ApplianceDto {
    private String id;
    private String name;
    private String category;
    private Integer wattage;
    private Integer quantity;
    private Double dailyHours;
    private Double monthlyKwh;

    public ApplianceDto() {}

    public ApplianceDto(String id, String name, String category, Integer wattage, Integer quantity, Double dailyHours, Double monthlyKwh) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.wattage = wattage;
        this.quantity = quantity;
        this.dailyHours = dailyHours;
        this.monthlyKwh = monthlyKwh;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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
}

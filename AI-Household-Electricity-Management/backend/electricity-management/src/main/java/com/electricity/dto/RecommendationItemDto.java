package com.electricity.dto;

public class RecommendationItemDto {
    private String id;
    private String category;
    private String title;
    private String action;
    private Double potentialSavingsKwh;
    private Double potentialSavingsInr;
    private String priority; 

    public RecommendationItemDto() {}

    public RecommendationItemDto(String id, String category, String title, String action,
                                 Double potentialSavingsKwh, Double potentialSavingsInr, String priority) {
        this.id = id;
        this.category = category;
        this.title = title;
        this.action = action;
        this.potentialSavingsKwh = potentialSavingsKwh;
        this.potentialSavingsInr = potentialSavingsInr;
        this.priority = priority;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Double getPotentialSavingsKwh() { return potentialSavingsKwh; }
    public void setPotentialSavingsKwh(Double potentialSavingsKwh) { this.potentialSavingsKwh = potentialSavingsKwh; }

    public Double getPotentialSavingsInr() { return potentialSavingsInr; }
    public void setPotentialSavingsInr(Double potentialSavingsInr) { this.potentialSavingsInr = potentialSavingsInr; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
}

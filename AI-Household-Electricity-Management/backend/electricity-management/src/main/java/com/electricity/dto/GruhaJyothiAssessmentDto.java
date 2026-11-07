package com.electricity.dto;

import java.util.List;

public class GruhaJyothiAssessmentDto {
    private String consumerNumber;
    private Integer householdSize;
    private Double historical12MonthAvgKwh;
    private Double entitledUnitsKwh;
    private Double maxCeilingKwh;
    private Double currentPredictedUnitsKwh;
    private String subsidyStatus; 
    private Boolean retainsBenefit;
    private Double estimatedMonthlySavingsInr;
    private Double estimatedNetPayableInr;
    private String guidanceMessage;
    private List<MonthlyForecastDto> monthlyForecasts;
    private List<RecommendationItemDto> recommendations;
    private Boolean isUserConfigured = true;

    private Double currentPartialKwh;
    private Integer daysElapsed;
    private Double dailyRunRateKwh;

    public GruhaJyothiAssessmentDto() {}

    public String getConsumerNumber() { return consumerNumber; }
    public void setConsumerNumber(String consumerNumber) { this.consumerNumber = consumerNumber; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Double getHistorical12MonthAvgKwh() { return historical12MonthAvgKwh; }
    public void setHistorical12MonthAvgKwh(Double historical12MonthAvgKwh) { this.historical12MonthAvgKwh = historical12MonthAvgKwh; }

    public Double getEntitledUnitsKwh() { return entitledUnitsKwh; }
    public void setEntitledUnitsKwh(Double entitledUnitsKwh) { this.entitledUnitsKwh = entitledUnitsKwh; }

    public Double getMaxCeilingKwh() { return maxCeilingKwh; }
    public void setMaxCeilingKwh(Double maxCeilingKwh) { this.maxCeilingKwh = maxCeilingKwh; }

    public Double getCurrentPredictedUnitsKwh() { return currentPredictedUnitsKwh; }
    public void setCurrentPredictedUnitsKwh(Double currentPredictedUnitsKwh) { this.currentPredictedUnitsKwh = currentPredictedUnitsKwh; }

    public String getSubsidyStatus() { return subsidyStatus; }
    public void setSubsidyStatus(String subsidyStatus) { this.subsidyStatus = subsidyStatus; }

    public Boolean getRetainsBenefit() { return retainsBenefit; }
    public void setRetainsBenefit(Boolean retainsBenefit) { this.retainsBenefit = retainsBenefit; }

    public Double getEstimatedMonthlySavingsInr() { return estimatedMonthlySavingsInr; }
    public void setEstimatedMonthlySavingsInr(Double estimatedMonthlySavingsInr) { this.estimatedMonthlySavingsInr = estimatedMonthlySavingsInr; }

    public Double getEstimatedNetPayableInr() { return estimatedNetPayableInr; }
    public void setEstimatedNetPayableInr(Double estimatedNetPayableInr) { this.estimatedNetPayableInr = estimatedNetPayableInr; }

    public String getGuidanceMessage() { return guidanceMessage; }
    public void setGuidanceMessage(String guidanceMessage) { this.guidanceMessage = guidanceMessage; }

    public List<MonthlyForecastDto> getMonthlyForecasts() { return monthlyForecasts; }
    public void setMonthlyForecasts(List<MonthlyForecastDto> monthlyForecasts) { this.monthlyForecasts = monthlyForecasts; }

    public List<RecommendationItemDto> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendationItemDto> recommendations) { this.recommendations = recommendations; }

    public Boolean getIsUserConfigured() { return isUserConfigured; }
    public void setIsUserConfigured(Boolean isUserConfigured) { this.isUserConfigured = isUserConfigured; }

    public Double getCurrentPartialKwh() { return currentPartialKwh; }
    public void setCurrentPartialKwh(Double currentPartialKwh) { this.currentPartialKwh = currentPartialKwh; }

    public Integer getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(Integer daysElapsed) { this.daysElapsed = daysElapsed; }

    public Double getDailyRunRateKwh() { return dailyRunRateKwh; }
    public void setDailyRunRateKwh(Double dailyRunRateKwh) { this.dailyRunRateKwh = dailyRunRateKwh; }
}

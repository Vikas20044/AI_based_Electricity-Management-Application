package com.electricity.dto;

public class MonthlyForecastDto {
    private String month;
    private Integer monthIndex;
    private String season;
    private Double temperatureC;
    private Double humidityPct;
    private Double predictedKwh;
    private Double estimatedBillInr;
    private Double subsidyBenefitInr;
    private Double netPayableInr;
    private String gruhaJyothiStatus; 
    private Boolean retainsBenefit;

    public MonthlyForecastDto() {}

    public MonthlyForecastDto(String month, Integer monthIndex, String season,
                              Double temperatureC, Double humidityPct, Double predictedKwh,
                              Double estimatedBillInr, Double subsidyBenefitInr,
                              Double netPayableInr, String gruhaJyothiStatus, Boolean retainsBenefit) {
        this.month = month;
        this.monthIndex = monthIndex;
        this.season = season;
        this.temperatureC = temperatureC;
        this.humidityPct = humidityPct;
        this.predictedKwh = predictedKwh;
        this.estimatedBillInr = estimatedBillInr;
        this.subsidyBenefitInr = subsidyBenefitInr;
        this.netPayableInr = netPayableInr;
        this.gruhaJyothiStatus = gruhaJyothiStatus;
        this.retainsBenefit = retainsBenefit;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }

    public Integer getMonthIndex() { return monthIndex; }
    public void setMonthIndex(Integer monthIndex) { this.monthIndex = monthIndex; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public Double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(Double temperatureC) { this.temperatureC = temperatureC; }

    public Double getHumidityPct() { return humidityPct; }
    public void setHumidityPct(Double humidityPct) { this.humidityPct = humidityPct; }

    public Double getPredictedKwh() { return predictedKwh; }
    public void setPredictedKwh(Double predictedKwh) { this.predictedKwh = predictedKwh; }

    public Double getEstimatedBillInr() { return estimatedBillInr; }
    public void setEstimatedBillInr(Double estimatedBillInr) { this.estimatedBillInr = estimatedBillInr; }

    public Double getSubsidyBenefitInr() { return subsidyBenefitInr; }
    public void setSubsidyBenefitInr(Double subsidyBenefitInr) { this.subsidyBenefitInr = subsidyBenefitInr; }

    public Double getNetPayableInr() { return netPayableInr; }
    public void setNetPayableInr(Double netPayableInr) { this.netPayableInr = netPayableInr; }

    public String getGruhaJyothiStatus() { return gruhaJyothiStatus; }
    public void setGruhaJyothiStatus(String gruhaJyothiStatus) { this.gruhaJyothiStatus = gruhaJyothiStatus; }

    public Boolean getRetainsBenefit() { return retainsBenefit; }
    public void setRetainsBenefit(Boolean retainsBenefit) { this.retainsBenefit = retainsBenefit; }
}

package com.electricity.integration.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLFeaturePayload {

    private String state = "Karnataka";
    private String district = "Tumakuru";

    @JsonProperty("area_type")
    private String areaType = "Urban";

    @JsonProperty("dwelling_type")
    private String dwellingType = "Apartment/flat";

    private String season = "Summer/Pre-monsoon";

    @JsonProperty("household_size")
    private Integer householdSize = 4;

    @JsonProperty("large_household_flag")
    private Integer largeHouseholdFlag = 0;

    private Integer year = 2024;

    @JsonProperty("month_number")
    private Integer monthNumber = 5;

    @JsonProperty("days_in_month")
    private Integer daysInMonth = 31;

    @JsonProperty("month_sin")
    private Double monthSin = 0.866;

    @JsonProperty("month_cos")
    private Double monthCos = -0.5;

    @JsonProperty("mean_temperature_c")
    private Double meanTemperatureC = 28.5;

    @JsonProperty("mean_relative_humidity_pct")
    private Double meanRelativeHumidityPct = 58.0;

    @JsonProperty("monthly_rainfall_mm")
    private Double monthlyRainfallMm = 45.0;

    @JsonProperty("previous_month_consumption_kwh")
    private Double previousMonthConsumptionKwh = 170.0;

    @JsonProperty("previous_2_month_consumption_kwh")
    private Double previous2MonthConsumptionKwh = 162.0;

    @JsonProperty("previous_3_month_consumption_kwh")
    private Double previous3MonthConsumptionKwh = 155.0;

    @JsonProperty("previous_month_bill_inr")
    private Double previousMonthBillInr = 1200.0;

    @JsonProperty("previous_2_month_bill_inr")
    private Double previous2MonthBillInr = 1150.0;

    @JsonProperty("previous_3_month_bill_inr")
    private Double previous3MonthBillInr = 1100.0;

    @JsonProperty("prior_3_month_avg_consumption_kwh")
    private Double prior3MonthAvgConsumptionKwh = 162.3;

    @JsonProperty("prior_3_month_avg_bill_inr")
    private Double prior3MonthAvgBillInr = 1150.0;

    @JsonProperty("prior_3_month_observation_count")
    private Integer prior3MonthObservationCount = 3;

    @JsonProperty("historical_mean_consumption_kwh")
    private Double historicalMeanConsumptionKwh = 159.0;

    @JsonProperty("historical_mean_bill_inr")
    private Double historicalMeanBillInr = 1130.0;

    @JsonProperty("historical_observations_count")
    private Integer historicalObservationsCount = 12;

    @JsonProperty("lag1_vs_lag2_consumption_change_kwh")
    private Double lag1VsLag2ConsumptionChangeKwh = 8.0;

    @JsonProperty("lag1_vs_lag2_consumption_change_pct")
    private Double lag1VsLag2ConsumptionChangePct = 4.94;

    @JsonProperty("current_partial_consumption_kwh")
    private Double currentPartialConsumptionKwh;

    @JsonProperty("days_elapsed")
    private Integer daysElapsed;

    @JsonProperty("sanctioned_load_kw")
    private Double sanctionedLoadKw = 2.0;

    @JsonProperty("appliance_count")
    private Integer applianceCount = 4;

    @JsonProperty("total_appliance_load_kw")
    private Double totalApplianceLoadKw = 1.5;

    @JsonProperty("appliance_monthly_kwh")
    private Double applianceMonthlyKwh = 140.0;

    @JsonProperty("heavy_appliance_flag")
    private Integer heavyApplianceFlag = 0;

    public MLFeaturePayload() {}

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public String getDwellingType() { return dwellingType; }
    public void setDwellingType(String dwellingType) { this.dwellingType = dwellingType; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Integer getLargeHouseholdFlag() { return largeHouseholdFlag; }
    public void setLargeHouseholdFlag(Integer largeHouseholdFlag) { this.largeHouseholdFlag = largeHouseholdFlag; }

    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }

    public Integer getMonthNumber() { return monthNumber; }
    public void setMonthNumber(Integer monthNumber) { this.monthNumber = monthNumber; }

    public Integer getDaysInMonth() { return daysInMonth; }
    public void setDaysInMonth(Integer daysInMonth) { this.daysInMonth = daysInMonth; }

    public Double getMonthSin() { return monthSin; }
    public void setMonthSin(Double monthSin) { this.monthSin = monthSin; }

    public Double getMonthCos() { return monthCos; }
    public void setMonthCos(Double monthCos) { this.monthCos = monthCos; }

    public Double getMeanTemperatureC() { return meanTemperatureC; }
    public void setMeanTemperatureC(Double meanTemperatureC) { this.meanTemperatureC = meanTemperatureC; }

    public Double getMeanRelativeHumidityPct() { return meanRelativeHumidityPct; }
    public void setMeanRelativeHumidityPct(Double meanRelativeHumidityPct) { this.meanRelativeHumidityPct = meanRelativeHumidityPct; }

    public Double getMonthlyRainfallMm() { return monthlyRainfallMm; }
    public void setMonthlyRainfallMm(Double monthlyRainfallMm) { this.monthlyRainfallMm = monthlyRainfallMm; }

    public Double getPreviousMonthConsumptionKwh() { return previousMonthConsumptionKwh; }
    public void setPreviousMonthConsumptionKwh(Double previousMonthConsumptionKwh) { this.previousMonthConsumptionKwh = previousMonthConsumptionKwh; }

    public Double getPrevious2MonthConsumptionKwh() { return previous2MonthConsumptionKwh; }
    public void setPrevious2MonthConsumptionKwh(Double previous2MonthConsumptionKwh) { this.previous2MonthConsumptionKwh = previous2MonthConsumptionKwh; }

    public Double getPrevious3MonthConsumptionKwh() { return previous3MonthConsumptionKwh; }
    public void setPrevious3MonthConsumptionKwh(Double previous3MonthConsumptionKwh) { this.previous3MonthConsumptionKwh = previous3MonthConsumptionKwh; }

    public Double getPreviousMonthBillInr() { return previousMonthBillInr; }
    public void setPreviousMonthBillInr(Double previousMonthBillInr) { this.previousMonthBillInr = previousMonthBillInr; }

    public Double getPrevious2MonthBillInr() { return previous2MonthBillInr; }
    public void setPrevious2MonthBillInr(Double previous2MonthBillInr) { this.previous2MonthBillInr = previous2MonthBillInr; }

    public Double getPrevious3MonthBillInr() { return previous3MonthBillInr; }
    public void setPrevious3MonthBillInr(Double previous3MonthBillInr) { this.previous3MonthBillInr = previous3MonthBillInr; }

    public Double getPrior3MonthAvgConsumptionKwh() { return prior3MonthAvgConsumptionKwh; }
    public void setPrior3MonthAvgConsumptionKwh(Double prior3MonthAvgConsumptionKwh) { this.prior3MonthAvgConsumptionKwh = prior3MonthAvgConsumptionKwh; }

    public Double getPrior3MonthAvgBillInr() { return prior3MonthAvgBillInr; }
    public void setPrior3MonthAvgBillInr(Double prior3MonthAvgBillInr) { this.prior3MonthAvgBillInr = prior3MonthAvgBillInr; }

    public Integer getPrior3MonthObservationCount() { return prior3MonthObservationCount; }
    public void setPrior3MonthObservationCount(Integer prior3MonthObservationCount) { this.prior3MonthObservationCount = prior3MonthObservationCount; }

    public Double getHistoricalMeanConsumptionKwh() { return historicalMeanConsumptionKwh; }
    public void setHistoricalMeanConsumptionKwh(Double historicalMeanConsumptionKwh) { this.historicalMeanConsumptionKwh = historicalMeanConsumptionKwh; }

    public Double getHistoricalMeanBillInr() { return historicalMeanBillInr; }
    public void setHistoricalMeanBillInr(Double historicalMeanBillInr) { this.historicalMeanBillInr = historicalMeanBillInr; }

    public Integer getHistoricalObservationsCount() { return historicalObservationsCount; }
    public void setHistoricalObservationsCount(Integer historicalObservationsCount) { this.historicalObservationsCount = historicalObservationsCount; }

    public Double getLag1VsLag2ConsumptionChangeKwh() { return lag1VsLag2ConsumptionChangeKwh; }
    public void setLag1VsLag2ConsumptionChangeKwh(Double lag1VsLag2ConsumptionChangeKwh) { this.lag1VsLag2ConsumptionChangeKwh = lag1VsLag2ConsumptionChangeKwh; }

    public Double getLag1VsLag2ConsumptionChangePct() { return lag1VsLag2ConsumptionChangePct; }
    public void setLag1VsLag2ConsumptionChangePct(Double lag1VsLag2ConsumptionChangePct) { this.lag1VsLag2ConsumptionChangePct = lag1VsLag2ConsumptionChangePct; }

    public Double getCurrentPartialConsumptionKwh() { return currentPartialConsumptionKwh; }
    public void setCurrentPartialConsumptionKwh(Double currentPartialConsumptionKwh) { this.currentPartialConsumptionKwh = currentPartialConsumptionKwh; }

    public Integer getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(Integer daysElapsed) { this.daysElapsed = daysElapsed; }

    public Double getSanctionedLoadKw() { return sanctionedLoadKw; }
    public void setSanctionedLoadKw(Double sanctionedLoadKw) { this.sanctionedLoadKw = sanctionedLoadKw; }

    public Integer getApplianceCount() { return applianceCount; }
    public void setApplianceCount(Integer applianceCount) { this.applianceCount = applianceCount; }

    public Double getTotalApplianceLoadKw() { return totalApplianceLoadKw; }
    public void setTotalApplianceLoadKw(Double totalApplianceLoadKw) { this.totalApplianceLoadKw = totalApplianceLoadKw; }

    public Double getApplianceMonthlyKwh() { return applianceMonthlyKwh; }
    public void setApplianceMonthlyKwh(Double applianceMonthlyKwh) { this.applianceMonthlyKwh = applianceMonthlyKwh; }

    public Integer getHeavyApplianceFlag() { return heavyApplianceFlag; }
    public void setHeavyApplianceFlag(Integer heavyApplianceFlag) { this.heavyApplianceFlag = heavyApplianceFlag; }
}

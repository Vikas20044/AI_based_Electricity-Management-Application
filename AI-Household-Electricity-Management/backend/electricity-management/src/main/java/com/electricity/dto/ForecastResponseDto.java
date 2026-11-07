package com.electricity.dto;

import java.util.List;

public class ForecastResponseDto {
    private MonthlyForecastDto currentMonth;
    private List<MonthlyForecastDto> upcoming12Months;
    private Double totalAnnualProjectedKwh;
    private Double totalAnnualEstimatedBillInr;
    private Double totalAnnualSubsidyBenefitInr;
    private Double totalAnnualNetPayableInr;
    private String modelAlgorithm;
    private Double householdSanctionedLoadKw;
    private Double entitlementLimitKwh;
    private Boolean isUserConfigured = true;

    private Double currentPartialKwh;
    private Integer daysElapsed;
    private Double dailyRunRateKwh;
    private Double projectedRemainingKwh;
    private Double ceilingMarginKwh;
    private Double entitlementMarginKwh;
    private String paceTrajectoryStatus;
    private String paceAdvisory;

    private Integer applianceCount;
    private Double totalApplianceLoadKw;
    private Double applianceMonthlyKwh;

    private Double monthlyBudgetInr = 2500.0;
    private Double budgetUtilizationPct;
    private Double budgetMarginInr;
    private String budgetStatus;
    private String budgetWarningMessage;

    public ForecastResponseDto() {}

    public MonthlyForecastDto getCurrentMonth() { return currentMonth; }
    public void setCurrentMonth(MonthlyForecastDto currentMonth) { this.currentMonth = currentMonth; }

    public List<MonthlyForecastDto> getUpcoming12Months() { return upcoming12Months; }
    public void setUpcoming12Months(List<MonthlyForecastDto> upcoming12Months) { this.upcoming12Months = upcoming12Months; }

    public Double getTotalAnnualProjectedKwh() { return totalAnnualProjectedKwh; }
    public void setTotalAnnualProjectedKwh(Double totalAnnualProjectedKwh) { this.totalAnnualProjectedKwh = totalAnnualProjectedKwh; }

    public Double getTotalAnnualEstimatedBillInr() { return totalAnnualEstimatedBillInr; }
    public void setTotalAnnualEstimatedBillInr(Double totalAnnualEstimatedBillInr) { this.totalAnnualEstimatedBillInr = totalAnnualEstimatedBillInr; }

    public Double getTotalAnnualSubsidyBenefitInr() { return totalAnnualSubsidyBenefitInr; }
    public void setTotalAnnualSubsidyBenefitInr(Double totalAnnualSubsidyBenefitInr) { this.totalAnnualSubsidyBenefitInr = totalAnnualSubsidyBenefitInr; }

    public Double getTotalAnnualNetPayableInr() { return totalAnnualNetPayableInr; }
    public void setTotalAnnualNetPayableInr(Double totalAnnualNetPayableInr) { this.totalAnnualNetPayableInr = totalAnnualNetPayableInr; }

    public String getModelAlgorithm() { return modelAlgorithm; }
    public void setModelAlgorithm(String modelAlgorithm) { this.modelAlgorithm = modelAlgorithm; }

    public Double getHouseholdSanctionedLoadKw() { return householdSanctionedLoadKw; }
    public void setHouseholdSanctionedLoadKw(Double householdSanctionedLoadKw) { this.householdSanctionedLoadKw = householdSanctionedLoadKw; }

    public Double getEntitlementLimitKwh() { return entitlementLimitKwh; }
    public void setEntitledLimitKwh(Double entitlementLimitKwh) { this.entitlementLimitKwh = entitlementLimitKwh; }

    public Boolean getIsUserConfigured() { return isUserConfigured; }
    public void setIsUserConfigured(Boolean isUserConfigured) { this.isUserConfigured = isUserConfigured; }

    public Double getCurrentPartialKwh() { return currentPartialKwh; }
    public void setCurrentPartialKwh(Double currentPartialKwh) { this.currentPartialKwh = currentPartialKwh; }

    public Integer getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(Integer daysElapsed) { this.daysElapsed = daysElapsed; }

    public Double getDailyRunRateKwh() { return dailyRunRateKwh; }
    public void setDailyRunRateKwh(Double dailyRunRateKwh) { this.dailyRunRateKwh = dailyRunRateKwh; }

    public Double getProjectedRemainingKwh() { return projectedRemainingKwh; }
    public void setProjectedRemainingKwh(Double projectedRemainingKwh) { this.projectedRemainingKwh = projectedRemainingKwh; }

    public Double getCeilingMarginKwh() { return ceilingMarginKwh; }
    public void setCeilingMarginKwh(Double ceilingMarginKwh) { this.ceilingMarginKwh = ceilingMarginKwh; }

    public Double getEntitlementMarginKwh() { return entitlementMarginKwh; }
    public void setEntitlementMarginKwh(Double entitlementMarginKwh) { this.entitlementMarginKwh = entitlementMarginKwh; }

    public String getPaceTrajectoryStatus() { return paceTrajectoryStatus; }
    public void setPaceTrajectoryStatus(String paceTrajectoryStatus) { this.paceTrajectoryStatus = paceTrajectoryStatus; }

    public String getPaceAdvisory() { return paceAdvisory; }
    public void setPaceAdvisory(String paceAdvisory) { this.paceAdvisory = paceAdvisory; }

    public Integer getApplianceCount() { return applianceCount; }
    public void setApplianceCount(Integer applianceCount) { this.applianceCount = applianceCount; }

    public Double getTotalApplianceLoadKw() { return totalApplianceLoadKw; }
    public void setTotalApplianceLoadKw(Double totalApplianceLoadKw) { this.totalApplianceLoadKw = totalApplianceLoadKw; }

    public Double getApplianceMonthlyKwh() { return applianceMonthlyKwh; }
    public void setApplianceMonthlyKwh(Double applianceMonthlyKwh) { this.applianceMonthlyKwh = applianceMonthlyKwh; }

    public Double getMonthlyBudgetInr() { return monthlyBudgetInr; }
    public void setMonthlyBudgetInr(Double monthlyBudgetInr) { this.monthlyBudgetInr = monthlyBudgetInr; }

    public Double getBudgetUtilizationPct() { return budgetUtilizationPct; }
    public void setBudgetUtilizationPct(Double budgetUtilizationPct) { this.budgetUtilizationPct = budgetUtilizationPct; }

    public Double getBudgetMarginInr() { return budgetMarginInr; }
    public void setBudgetMarginInr(Double budgetMarginInr) { this.budgetMarginInr = budgetMarginInr; }

    public String getBudgetStatus() { return budgetStatus; }
    public void setBudgetStatus(String budgetStatus) { this.budgetStatus = budgetStatus; }

    public String getBudgetWarningMessage() { return budgetWarningMessage; }
    public void setBudgetWarningMessage(String budgetWarningMessage) { this.budgetWarningMessage = budgetWarningMessage; }
}

package com.electricity.service;

import com.electricity.billing.BillingService;
import com.electricity.dto.ForecastResponseDto;
import com.electricity.dto.MonthlyForecastDto;
import com.electricity.entity.Appliance;
import com.electricity.entity.Household;
import com.electricity.entity.Prediction;
import com.electricity.entity.User;
import com.electricity.integration.ml.MLFeaturePayload;
import com.electricity.integration.ml.MLPredictionResponse;
import com.electricity.integration.ml.MLServiceClient;
import com.electricity.repository.ApplianceRepository;
import com.electricity.repository.HouseholdRepository;
import com.electricity.repository.PredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForecastService {

    private final MLServiceClient mlServiceClient;
    private final BillingService billingService;
    private final HouseholdService householdService;
    private final HouseholdRepository householdRepository;
    private final ApplianceRepository applianceRepository;
    private final PredictionRepository predictionRepository;

    public ForecastService(MLServiceClient mlServiceClient,
                           BillingService billingService,
                           HouseholdService householdService,
                           HouseholdRepository householdRepository,
                           ApplianceRepository applianceRepository,
                           PredictionRepository predictionRepository) {
        this.mlServiceClient = mlServiceClient;
        this.billingService = billingService;
        this.householdService = householdService;
        this.householdRepository = householdRepository;
        this.applianceRepository = applianceRepository;
        this.predictionRepository = predictionRepository;
    }

    public ForecastResponseDto generate12MonthForecast(User user) {
        return generate12MonthForecast(user, null, null, null);
    }

    public ForecastResponseDto generate12MonthForecast(User user, Double customPartialKwh, Integer customDaysElapsed) {
        return generate12MonthForecast(user, customPartialKwh, customDaysElapsed, null);
    }

    @Transactional
    public ForecastResponseDto generate12MonthForecast(User user, Double customPartialKwh, Integer customDaysElapsed, Double customBudgetInr) {
        Household hh = householdService.getOrCreateDefaultHousehold(user);
        List<Appliance> apps = applianceRepository.findByHouseholdId(hh.getId());

        double totalApplianceKwh = apps.stream()
                .mapToDouble(a -> a.getMonthlyKwh() != null ? a.getMonthlyKwh() : 0.0)
                .sum();

        double pastUnits = hh.getPastMonthUnitsKwh() != null && hh.getPastMonthUnitsKwh() > 0
                ? hh.getPastMonthUnitsKwh()
                : (totalApplianceKwh > 50.0 ? totalApplianceKwh : 160.0);

        LocalDate today = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMM yyyy");

        int days = (customDaysElapsed != null && customDaysElapsed > 0 && customDaysElapsed <= 31)
                ? customDaysElapsed
                : (hh.getDaysElapsed() != null && hh.getDaysElapsed() > 0 ? hh.getDaysElapsed() : 15);

        double partial = (customPartialKwh != null && customPartialKwh >= 0)
                ? customPartialKwh
                : (hh.getCurrentPartialKwh() != null && hh.getCurrentPartialKwh() >= 0
                    ? hh.getCurrentPartialKwh()
                    : Math.round((pastUnits * (days / 30.0)) * 10.0) / 10.0);

        double budget = (customBudgetInr != null && customBudgetInr > 0)
                ? customBudgetInr
                : (hh.getMonthlyBudgetInr() != null && hh.getMonthlyBudgetInr() > 0 ? hh.getMonthlyBudgetInr() : 2500.0);

        hh.setCurrentPartialKwh(partial);
        hh.setDaysElapsed(days);
        hh.setMonthlyBudgetInr(budget);
        householdRepository.save(hh);

        int applianceCount = apps.stream()
                .mapToInt(a -> a.getQuantity() != null ? a.getQuantity() : 1)
                .sum();

        double totalApplianceWattage = apps.stream()
                .mapToDouble(a -> (a.getWattage() != null ? a.getWattage() : 0) * (a.getQuantity() != null ? a.getQuantity() : 1))
                .sum();
        double totalApplianceLoadKw = Math.round((totalApplianceWattage / 1000.0) * 100.0) / 100.0;

        boolean hasHeavy = apps.stream().anyMatch(a -> {
            String n = a.getName() != null ? a.getName().toLowerCase() : "";
            String c = a.getCategory() != null ? a.getCategory().toLowerCase() : "";
            int w = a.getWattage() != null ? a.getWattage() : 0;
            return n.contains("conditioner") || n.contains("ac") || n.contains("geyser") || n.contains("heater") || w >= 1200;
        });
        int heavyFlag = hasHeavy ? 1 : 0;

        MLFeaturePayload payload = new MLFeaturePayload();
        payload.setState(hh.getState());
        payload.setDistrict(hh.getDistrict());
        payload.setAreaType(hh.getAreaType());
        payload.setDwellingType(hh.getDwellingType());
        payload.setSeason("Post-monsoon");
        payload.setHouseholdSize(hh.getHouseholdSize());
        payload.setPreviousMonthConsumptionKwh(pastUnits);
        payload.setCurrentPartialConsumptionKwh(partial);
        payload.setDaysElapsed(days);
        payload.setSanctionedLoadKw(hh.getSanctionedLoadKw());
        payload.setApplianceCount(applianceCount > 0 ? applianceCount : 4);
        payload.setTotalApplianceLoadKw(totalApplianceLoadKw > 0 ? totalApplianceLoadKw : 1.5);
        payload.setApplianceMonthlyKwh(totalApplianceKwh > 0 ? totalApplianceKwh : pastUnits);
        payload.setHeavyApplianceFlag(heavyFlag);

        MLPredictionResponse mlResp = mlServiceClient.requestPrediction(payload);
        double mlBaseKwh = mlResp.getPredictedConsumptionKwh() != null ? mlResp.getPredictedConsumptionKwh() : pastUnits;
        String modelName = mlResp.getModelAlgorithm() != null ? mlResp.getModelAlgorithm() : "Long Short-Term Memory (LSTM) Deep Learning Regressor";

        MonthlyForecastDto currentMonthDto = buildMonthlyForecast(
                today.format(monthFormatter) + " (Current)",
                0,
                today.getMonthValue(),
                mlBaseKwh,
                hh
        );

        Prediction pred = new Prediction();
        pred.setHousehold(hh);
        pred.setTargetMonth(today.format(DateTimeFormatter.ofPattern("yyyy-MM")));
        pred.setPredictedMonthlyKwh(currentMonthDto.getPredictedKwh());
        pred.setEstimatedBillInr(currentMonthDto.getEstimatedBillInr());
        pred.setGruhaJyothiStatus(currentMonthDto.getGruhaJyothiStatus());
        pred.setSubsidyBenefitInr(currentMonthDto.getSubsidyBenefitInr());
        pred.setNetPayableInr(currentMonthDto.getNetPayableInr());
        pred.setModelAlgorithm(modelName);
        pred.setWeatherTemperatureC(currentMonthDto.getTemperatureC());
        pred.setWeatherHumidityPct(currentMonthDto.getHumidityPct());
        pred.setSeason(currentMonthDto.getSeason());
        pred.setPredictionDate(LocalDateTime.now());
        predictionRepository.save(pred);

        List<MonthlyForecastDto> upcomingList = new ArrayList<>();
        double totalAnnualKwh = 0.0;
        double totalAnnualGross = 0.0;
        double totalAnnualSubsidy = 0.0;
        double totalAnnualNet = 0.0;

        double prevMonthKwh = currentMonthDto.getPredictedKwh();
        for (int i = 1; i <= 12; i++) {
            LocalDate futureDate = today.plusMonths(i);
            String monthLabel = futureDate.format(monthFormatter);
            int monthNum = futureDate.getMonthValue();

            double futureKwh;
            try {
                MLFeaturePayload futurePayload = new MLFeaturePayload();
                futurePayload.setState(hh.getState() != null ? hh.getState() : "Karnataka");
                futurePayload.setDistrict(hh.getDistrict() != null ? hh.getDistrict() : "Tumakuru");
                futurePayload.setAreaType(hh.getAreaType() != null ? hh.getAreaType() : "Urban");
                futurePayload.setDwellingType(hh.getDwellingType() != null ? hh.getDwellingType() : "2 BHK");
                futurePayload.setHouseholdSize(hh.getHouseholdSize() != null ? hh.getHouseholdSize() : 4);
                futurePayload.setYear(futureDate.getYear());
                futurePayload.setMonthNumber(monthNum);
                futurePayload.setPreviousMonthConsumptionKwh(prevMonthKwh);
                futurePayload.setSanctionedLoadKw(hh.getSanctionedLoadKw() != null ? hh.getSanctionedLoadKw() : 2.0);
                futurePayload.setApplianceCount(applianceCount > 0 ? applianceCount : 4);
                futurePayload.setTotalApplianceLoadKw(totalApplianceLoadKw > 0 ? totalApplianceLoadKw : 1.5);
                futurePayload.setApplianceMonthlyKwh(totalApplianceKwh > 0 ? totalApplianceKwh : pastUnits);
                futurePayload.setHeavyApplianceFlag(heavyFlag);
                futurePayload.setCurrentPartialConsumptionKwh(null);
                futurePayload.setDaysElapsed(null);

                MLPredictionResponse futureMlResp = mlServiceClient.requestPrediction(futurePayload);
                if (futureMlResp != null && futureMlResp.getPredictedConsumptionKwh() != null && futureMlResp.getPredictedConsumptionKwh() > 0) {
                    futureKwh = futureMlResp.getPredictedConsumptionKwh();
                } else {
                    futureKwh = calculateSeasonalKwh(mlBaseKwh, i, monthNum);
                }
            } catch (Exception ex) {
                futureKwh = calculateSeasonalKwh(mlBaseKwh, i, monthNum);
            }

            MonthlyForecastDto monthDto = buildMonthlyForecastWithKwh(
                    monthLabel,
                    i,
                    monthNum,
                    futureKwh,
                    hh
            );

            upcomingList.add(monthDto);
            totalAnnualKwh += monthDto.getPredictedKwh();
            totalAnnualGross += monthDto.getEstimatedBillInr();
            totalAnnualSubsidy += monthDto.getSubsidyBenefitInr();
            totalAnnualNet += monthDto.getNetPayableInr();

            prevMonthKwh = futureKwh;
        }

        double dailyBurnRate = round(partial / days);
        double remainingKwh = mlResp.getProjectedRemainingKwh() != null
                ? mlResp.getProjectedRemainingKwh()
                : Math.max(0.0, round(mlBaseKwh - partial));

        double entitledLimit = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 176.0;
        double ceilingMargin = round(200.0 - mlBaseKwh);
        double entitlementMargin = round(entitledLimit - mlBaseKwh);

        String trajectoryStatus;
        String advisory;
        if (mlBaseKwh <= entitledLimit && mlBaseKwh <= 200.0) {
            trajectoryStatus = "ON_TRACK_ZERO_BILL";
            advisory = String.format("Current burn pace (%.2f kWh/day) is well within limit. Projected end-of-month usage of %.1f kWh will retain 100%% Zero Bill subsidy under Gruha Jyothi.", dailyBurnRate, mlBaseKwh);
        } else if (mlBaseKwh > entitledLimit && mlBaseKwh <= 200.0) {
            trajectoryStatus = "APPROACHING_LIMIT";
            advisory = String.format("Caution: At %.2f kWh/day, projected consumption (%.1f kWh) exceeds entitlement (%.1f kWh) by %.1f units. You will be billed for excess units.", dailyBurnRate, mlBaseKwh, entitledLimit, mlBaseKwh - entitledLimit);
        } else {
            trajectoryStatus = "CEILING_BREACH_RISK";
            advisory = String.format("Alert: Current pace of %.2f kWh/day risks exceeding Karnataka's 200 kWh statutory ceiling (Projected: %.1f kWh). Subsidy will be completely forfeited unless consumption is curtailed.", dailyBurnRate, mlBaseKwh);
        }

        ForecastResponseDto response = new ForecastResponseDto();
        response.setCurrentMonth(currentMonthDto);
        response.setUpcoming12Months(upcomingList);
        response.setTotalAnnualProjectedKwh(round(totalAnnualKwh));
        response.setTotalAnnualEstimatedBillInr(round(totalAnnualGross));
        response.setTotalAnnualSubsidyBenefitInr(round(totalAnnualSubsidy));
        response.setTotalAnnualNetPayableInr(round(totalAnnualNet));
        response.setModelAlgorithm(modelName);
        response.setHouseholdSanctionedLoadKw(hh.getSanctionedLoadKw());
        response.setEntitledLimitKwh(entitledLimit);

        response.setCurrentPartialKwh(round(partial));
        response.setDaysElapsed(days);
        response.setDailyRunRateKwh(dailyBurnRate);
        response.setProjectedRemainingKwh(round(remainingKwh));
        response.setCeilingMarginKwh(ceilingMargin);
        response.setEntitlementMarginKwh(entitlementMargin);
        response.setPaceTrajectoryStatus(trajectoryStatus);
        response.setPaceAdvisory(advisory);

        response.setApplianceCount(applianceCount > 0 ? applianceCount : 4);
        response.setTotalApplianceLoadKw(totalApplianceLoadKw > 0 ? totalApplianceLoadKw : 1.5);
        response.setApplianceMonthlyKwh(round(totalApplianceKwh > 0 ? totalApplianceKwh : pastUnits));

        double estimatedBill = currentMonthDto.getEstimatedBillInr();
        double budgetUtilization = Math.round((estimatedBill / budget) * 1000.0) / 10.0;
        double budgetMargin = Math.round((budget - estimatedBill) * 100.0) / 100.0;

        String budgetStatus;
        String budgetWarningMessage;

        if (budgetUtilization < 80.0) {
            budgetStatus = "WITHIN_BUDGET";
            budgetWarningMessage = String.format("Safe: Projected electricity bill of ₹%.2f is well within your monthly budget threshold of ₹%.2f (%.1f%% utilized). Remaining buffer is ₹%.2f.",
                    estimatedBill, budget, budgetUtilization, budgetMargin);
        } else if (budgetUtilization <= 100.0) {
            budgetStatus = "APPROACHING_BUDGET";
            budgetWarningMessage = String.format("Warning: Projected electricity bill of ₹%.2f is approaching your monthly budget threshold of ₹%.2f (%.1f%% utilized). Remaining buffer is only ₹%.2f. Consider moderating appliance usage.",
                    estimatedBill, budget, budgetUtilization, budgetMargin);
        } else {
            budgetStatus = "BUDGET_EXCEEDED";
            budgetWarningMessage = String.format("Alert: Model forecasts your electricity bill of ₹%.2f will cross your budget threshold of ₹%.2f by ₹%.2f (%.1f%% of budget)! Immediate load reduction recommended.",
                    estimatedBill, budget, Math.abs(budgetMargin), budgetUtilization);
        }

        response.setMonthlyBudgetInr(budget);
        response.setBudgetUtilizationPct(budgetUtilization);
        response.setBudgetMarginInr(budgetMargin);
        response.setBudgetStatus(budgetStatus);
        response.setBudgetWarningMessage(budgetWarningMessage);

        return response;
    }

    private MonthlyForecastDto buildMonthlyForecast(String monthLabel, int monthIndex, int monthNum,
                                                    double baseKwh, Household hh) {
        double predictedKwh = (monthIndex == 0) ? round(baseKwh) : calculateSeasonalKwh(baseKwh, monthIndex, monthNum);
        return buildMonthlyForecastWithKwh(monthLabel, monthIndex, monthNum, predictedKwh, hh);
    }

    private MonthlyForecastDto buildMonthlyForecastWithKwh(String monthLabel, int monthIndex, int monthNum,
                                                           double predictedKwh, Household hh) {
        String season;
        double temp;
        double humidity;

        if (monthNum >= 3 && monthNum <= 5) {
            season = "Summer";
            temp = 33.5;
            humidity = 48.0;
        } else if (monthNum >= 6 && monthNum <= 9) {
            season = "Monsoon";
            temp = 26.0;
            humidity = 78.0;
        } else if (monthNum >= 10 && monthNum <= 11) {
            season = "Post-monsoon";
            temp = 27.5;
            humidity = 65.0;
        } else {
            season = "Winter";
            temp = 23.5;
            humidity = 54.0;
        }

        double entitledUnits = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 175.0;
        double sanctionedLoad = hh.getSanctionedLoadKw() != null ? hh.getSanctionedLoadKw() : 2.0;

        BillingService.BillCalculation calc = billingService.calculateBill(
                round(predictedKwh),
                sanctionedLoad,
                entitledUnits,
                Boolean.TRUE.equals(hh.getSchemeRegistered())
        );

        boolean retains = !"EXCEEDED".equalsIgnoreCase(calc.getSubsidyStatus());

        return new MonthlyForecastDto(
                monthLabel,
                monthIndex,
                season,
                temp,
                humidity,
                round(predictedKwh),
                calc.getGrossBill(),
                calc.getSubsidyDeduction(),
                calc.getNetPayable(),
                calc.getSubsidyStatus(),
                retains
        );
    }

    private double calculateSeasonalKwh(double baseKwh, int monthIndex, int monthNum) {
        double seasonalMultiplier;
        if (monthNum >= 3 && monthNum <= 5) {
            seasonalMultiplier = 1.15;
        } else if (monthNum >= 6 && monthNum <= 9) {
            seasonalMultiplier = 0.94;
        } else if (monthNum >= 10 && monthNum <= 11) {
            seasonalMultiplier = 0.98;
        } else {
            seasonalMultiplier = 0.93;
        }
        double trendFactor = 1.0 + (monthIndex * 0.003);
        return round(baseKwh * seasonalMultiplier * trendFactor);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}

package com.electricity.service;

import com.electricity.dto.ForecastResponseDto;
import com.electricity.dto.GruhaJyothiAssessmentDto;
import com.electricity.dto.MonthlyForecastDto;
import com.electricity.dto.RecommendationItemDto;
import com.electricity.entity.Appliance;
import com.electricity.entity.Household;
import com.electricity.entity.User;
import com.electricity.repository.ApplianceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GruhaJyothiService {

    private final HouseholdService householdService;
    private final ForecastService forecastService;
    private final RecommendationService recommendationService;
    private final ApplianceRepository applianceRepository;

    public GruhaJyothiService(HouseholdService householdService,
                              ForecastService forecastService,
                              RecommendationService recommendationService,
                              ApplianceRepository applianceRepository) {
        this.householdService = householdService;
        this.forecastService = forecastService;
        this.recommendationService = recommendationService;
        this.applianceRepository = applianceRepository;
    }

    public GruhaJyothiAssessmentDto evaluateSubsidyRetention(User user) {
        return evaluateSubsidyRetention(user, null, null);
    }

    public GruhaJyothiAssessmentDto evaluateSubsidyRetention(User user, Double partialKwh, Integer daysElapsed) {
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        Double actualPartial = partialKwh != null ? partialKwh : hh.getCurrentPartialKwh();
        Integer actualDays = daysElapsed != null ? daysElapsed : hh.getDaysElapsed();

        ForecastResponseDto forecast = forecastService.generate12MonthForecast(user, actualPartial, actualDays);
        List<Appliance> apps = applianceRepository.findByHouseholdId(hh.getId());

        MonthlyForecastDto current = forecast.getCurrentMonth();
        double predictedKwh = current.getPredictedKwh();
        double entitledUnits = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 175.0;
        double ceiling = 200.0;

        String status;
        boolean retains;
        String guidance;

        if (predictedKwh <= entitledUnits && predictedKwh <= ceiling) {
            status = "ELIGIBLE";
            retains = true;
            guidance = String.format(
                    "Congratulations! Based on your active mid-month tracking (%.1f kWh in %d days), your projected consumption of %.1f kWh is within your entitlement limit of %.1f kWh. " +
                    "Your household retains 100%% Zero Bill benefit for the active billing cycle (Saving ₹%.2f).",
                    forecast.getCurrentPartialKwh() != null ? forecast.getCurrentPartialKwh() : (predictedKwh * 0.5),
                    forecast.getDaysElapsed() != null ? forecast.getDaysElapsed() : 15,
                    predictedKwh, entitledUnits, current.getSubsidyBenefitInr()
            );
        } else if (predictedKwh > entitledUnits && predictedKwh <= ceiling) {
            status = "APPROACHING_THRESHOLD";
            retains = true;
            double excess = Math.round((predictedKwh - entitledUnits) * 10.0) / 10.0;
            guidance = String.format(
                    "Caution: Based on your active mid-month tracking, projected consumption of %.1f kWh exceeds your entitlement limit of %.1f kWh by %.1f units. " +
                    "You will retain the subsidy on %.1f units, but will be billed ₹%.2f for the excess units at standard tariff rates.",
                    predictedKwh, entitledUnits, excess, entitledUnits, current.getNetPayableInr()
            );
        } else {
            status = "EXCEEDED";
            retains = false;
            guidance = String.format(
                    "Warning: Based on your active mid-month tracking, projected consumption of %.1f kWh exceeds Karnataka's 200-unit ceiling limit. " +
                    "Gruha Jyothi welfare subsidy is completely forfeited for this billing cycle, and a full commercial bill of ₹%.2f is payable.",
                    predictedKwh, current.getEstimatedBillInr()
            );
        }

        List<RecommendationItemDto> recommendations = recommendationService.generateRecommendations(apps, predictedKwh, entitledUnits);

        GruhaJyothiAssessmentDto dto = new GruhaJyothiAssessmentDto();
        dto.setConsumerNumber(hh.getConsumerNumber());
        dto.setHouseholdSize(hh.getHouseholdSize());
        dto.setHistorical12MonthAvgKwh(hh.getHistorical12MonthAvgKwh());
        dto.setEntitledUnitsKwh(entitledUnits);
        dto.setMaxCeilingKwh(ceiling);
        dto.setCurrentPredictedUnitsKwh(predictedKwh);
        dto.setSubsidyStatus(status);
        dto.setRetainsBenefit(retains);
        dto.setEstimatedMonthlySavingsInr(current.getSubsidyBenefitInr());
        dto.setEstimatedNetPayableInr(current.getNetPayableInr());
        dto.setGuidanceMessage(guidance);
        dto.setMonthlyForecasts(forecast.getUpcoming12Months());
        dto.setRecommendations(recommendations);
        dto.setCurrentPartialKwh(forecast.getCurrentPartialKwh());
        dto.setDaysElapsed(forecast.getDaysElapsed());
        dto.setDailyRunRateKwh(forecast.getDailyRunRateKwh());

        return dto;
    }
}

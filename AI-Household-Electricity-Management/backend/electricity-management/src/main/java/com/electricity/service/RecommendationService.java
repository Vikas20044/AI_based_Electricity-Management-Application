package com.electricity.service;

import com.electricity.dto.RecommendationItemDto;
import com.electricity.entity.Appliance;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    public List<RecommendationItemDto> generateRecommendations(List<Appliance> appliances, double currentPredictedKwh, double entitlementLimitKwh) {
        List<RecommendationItemDto> recs = new ArrayList<>();

        double ceilingTarget = 200.0;
        double reductionNeededForCeiling = Math.max(0.0, Math.round((currentPredictedKwh - ceilingTarget + 5.0) * 10.0) / 10.0);
        double reductionNeededForZeroBill = Math.max(0.0, Math.round((currentPredictedKwh - entitlementLimitKwh) * 10.0) / 10.0);

        boolean hasGeyser = appliances.stream().anyMatch(a -> a.getName().toLowerCase().contains("geyser") || a.getCategory().equalsIgnoreCase("Heating"));
        if (hasGeyser || currentPredictedKwh > 140.0) {
            double savingsKwh = 22.5;
            double savingsInr = Math.round(savingsKwh * 7.0 * 1.09);
            recs.add(new RecommendationItemDto(
                    "rec-1",
                    "Water Heating",
                    "Optimize Water Geyser Runtime to 25 Mins",
                    "Restrict electric geyser to 25 minutes prior to bathing instead of keeping it continuously on thermostat. Saves ~22.5 kWh/month.",
                    savingsKwh,
                    savingsInr,
                    currentPredictedKwh > 175.0 ? "HIGH" : "MEDIUM"
            ));
        }

        boolean hasFans = appliances.stream().anyMatch(a -> a.getName().toLowerCase().contains("fan") || a.getCategory().equalsIgnoreCase("Cooling"));
        if (hasFans || currentPredictedKwh > 120.0) {
            double savingsKwh = 15.0;
            double savingsInr = Math.round(savingsKwh * 7.0 * 1.09);
            recs.add(new RecommendationItemDto(
                    "rec-2",
                    "Cooling Efficiency",
                    "Shift Ceiling Fan Speed from 5 to 3 in Empty Rooms",
                    "Operating 3 ceiling fans at speed 3 instead of maximum speed 5 and turning off in unoccupied rooms reduces load by ~15.0 kWh/month.",
                    savingsKwh,
                    savingsInr,
                    "MEDIUM"
            ));
        }

        double savingsVampireKwh = 8.0;
        double savingsVampireInr = Math.round(savingsVampireKwh * 7.0 * 1.09);
        recs.add(new RecommendationItemDto(
                "rec-3",
                "Standby Power",
                "Eliminate Electronics Vampire Draw at Wall Socket",
                "Switch off televisions, Wi-Fi routers at night, microwave displays, and phone chargers at the switchboard. Stops ~8.0 kWh phantom draw.",
                savingsVampireKwh,
                savingsVampireInr,
                "LOW"
        ));

        if (currentPredictedKwh > 185.0) {
            double urgentSavings = Math.max(12.0, reductionNeededForCeiling);
            double urgentInr = Math.round(urgentSavings * 8.0 * 1.09);
            recs.add(0, new RecommendationItemDto(
                    "rec-urgent",
                    "Ceiling Protection Alert",
                    String.format("Target Immediate %.1f kWh Daily Curtailment", urgentSavings / 10.0),
                    String.format("Your forecast is %.1f kWh. Curtailing high-wattage appliance usage by %.1f units keeps you strictly under Karnataka's 200 kWh ceiling, safeguarding your subsidy.", currentPredictedKwh, urgentSavings),
                    urgentSavings,
                    urgentInr,
                    "HIGH"
            ));
        } else if (reductionNeededForZeroBill > 0) {
            double zeroBillSavings = reductionNeededForZeroBill;
            double zeroBillInr = Math.round(zeroBillSavings * 7.0 * 1.09);
            recs.add(0, new RecommendationItemDto(
                    "rec-zerobill",
                    "100% Zero Bill Target",
                    String.format("Reduce %.1f kWh to Restore Complete Zero Bill", zeroBillSavings),
                    String.format("Shaving %.1f units from your monthly total will bring your usage below your %.1f kWh entitlement limit, eliminating all out-of-pocket charges.", zeroBillSavings, entitlementLimitKwh),
                    zeroBillSavings,
                    zeroBillInr,
                    "HIGH"
            ));
        }

        return recs;
    }
}

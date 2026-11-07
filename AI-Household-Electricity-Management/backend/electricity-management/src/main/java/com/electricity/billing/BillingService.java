package com.electricity.billing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class BillingService {

    private final double fixedChargePerKw;
    private final int slab1MaxUnits;
    private final double slab1Rate;
    private final int slab2MaxUnits;
    private final double slab2Rate;
    private final double slab3Rate;
    private final double taxPct;

    public BillingService(
            @Value("${tariff.fixed-charge-per-kw-inr:70.0}") double fixedChargePerKw,
            @Value("${tariff.slab1-max-units:100}") int slab1MaxUnits,
            @Value("${tariff.slab1-rate-per-unit:4.75}") double slab1Rate,
            @Value("${tariff.slab2-max-units:200}") int slab2MaxUnits,
            @Value("${tariff.slab2-rate-per-unit:7.00}") double slab2Rate,
            @Value("${tariff.slab3-rate-per-unit:8.00}") double slab3Rate,
            @Value("${tariff.electricity-tax-pct:9.0}") double taxPct) {
        this.fixedChargePerKw = fixedChargePerKw;
        this.slab1MaxUnits = slab1MaxUnits;
        this.slab1Rate = slab1Rate;
        this.slab2MaxUnits = slab2MaxUnits;
        this.slab2Rate = slab2Rate;
        this.slab3Rate = slab3Rate;
        this.taxPct = taxPct;
    }

    public static class BillCalculation {
        private final double unitsKwh;
        private final double fixedCharges;
        private final double energyCharges;
        private final double taxAmount;
        private final double grossBill;
        private final double subsidyDeduction;
        private final double netPayable;
        private final String subsidyStatus;

        public BillCalculation(double unitsKwh, double fixedCharges, double energyCharges,
                               double taxAmount, double grossBill, double subsidyDeduction,
                               double netPayable, String subsidyStatus) {
            this.unitsKwh = round(unitsKwh);
            this.fixedCharges = round(fixedCharges);
            this.energyCharges = round(energyCharges);
            this.taxAmount = round(taxAmount);
            this.grossBill = round(grossBill);
            this.subsidyDeduction = round(subsidyDeduction);
            this.netPayable = round(netPayable);
            this.subsidyStatus = subsidyStatus;
        }

        private static double round(double val) {
            return Math.round(val * 100.0) / 100.0;
        }

        public double getUnitsKwh() { return unitsKwh; }
        public double getFixedCharges() { return fixedCharges; }
        public double getEnergyCharges() { return energyCharges; }
        public double getTaxAmount() { return taxAmount; }
        public double getGrossBill() { return grossBill; }
        public double getSubsidyDeduction() { return subsidyDeduction; }
        public double getNetPayable() { return netPayable; }
        public String getSubsidyStatus() { return subsidyStatus; }
    }

    public BillCalculation calculateBill(double unitsKwh, double sanctionedLoadKw, double entitledUnitsKwh, boolean schemeRegistered) {
        double fixedCharges = sanctionedLoadKw * fixedChargePerKw;
        double energyCharges = 0.0;

        if (unitsKwh <= slab1MaxUnits) {
            energyCharges = unitsKwh * slab1Rate;
        } else if (unitsKwh <= slab2MaxUnits) {
            energyCharges = (slab1MaxUnits * slab1Rate) + ((unitsKwh - slab1MaxUnits) * slab2Rate);
        } else {
            energyCharges = (slab1MaxUnits * slab1Rate) + ((slab2MaxUnits - slab1MaxUnits) * slab2Rate)
                    + ((unitsKwh - slab2MaxUnits) * slab3Rate);
        }

        double taxAmount = (fixedCharges + energyCharges) * (taxPct / 100.0);
        double grossBill = fixedCharges + energyCharges + taxAmount;

        double subsidyDeduction = 0.0;
        double netPayable = grossBill;
        String subsidyStatus = "NOT_REGISTERED";

        if (schemeRegistered) {
            if (unitsKwh <= entitledUnitsKwh && unitsKwh <= 200.0) {

                subsidyStatus = "ELIGIBLE";
                subsidyDeduction = grossBill;
                netPayable = 0.0;
            } else if (unitsKwh > entitledUnitsKwh && unitsKwh <= 200.0) {

                subsidyStatus = "APPROACHING_THRESHOLD";
                double excessUnits = unitsKwh - entitledUnitsKwh;
                netPayable = excessUnits * slab2Rate * (1.0 + (taxPct / 100.0));
                subsidyDeduction = Math.max(0.0, grossBill - netPayable);
            } else {

                subsidyStatus = "EXCEEDED";
                subsidyDeduction = 0.0;
                netPayable = grossBill;
            }
        }

        return new BillCalculation(unitsKwh, fixedCharges, energyCharges, taxAmount, grossBill, subsidyDeduction, netPayable, subsidyStatus);
    }
}

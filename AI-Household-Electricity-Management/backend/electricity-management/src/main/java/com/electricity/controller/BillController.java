package com.electricity.controller;

import com.electricity.billing.BillingService;
import com.electricity.dto.ApiResponse;
import com.electricity.dto.PastMonthBillDto;
import com.electricity.entity.Household;
import com.electricity.entity.User;
import com.electricity.repository.HouseholdRepository;
import com.electricity.service.HouseholdService;
import com.electricity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bills")
@CrossOrigin(origins = "*")
public class BillController {

    private final HouseholdService householdService;
    private final HouseholdRepository householdRepository;
    private final BillingService billingService;
    private final UserService userService;

    public BillController(HouseholdService householdService,
                          HouseholdRepository householdRepository,
                          BillingService billingService,
                          UserService userService) {
        this.householdService = householdService;
        this.householdRepository = householdRepository;
        this.billingService = billingService;
        this.userService = userService;
    }

    @GetMapping("/past-month")
    public ResponseEntity<PastMonthBillDto> getPastMonthBill() {
        User user = userService.getAuthenticatedUser();
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        double units = hh.getPastMonthUnitsKwh() != null ? hh.getPastMonthUnitsKwh() : 160.0;
        double load = hh.getSanctionedLoadKw() != null ? hh.getSanctionedLoadKw() : 2.0;
        double entitled = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 176.0;

        BillingService.BillCalculation calc = billingService.calculateBill(units, load, entitled, true);

        PastMonthBillDto dto = new PastMonthBillDto(
                hh.getPastMonthName() != null ? hh.getPastMonthName() : "October 2026",
                units,
                calc.getGrossBill(),
                calc.getFixedCharges(),
                calc.getEnergyCharges(),
                calc.getTaxAmount(),
                calc.getSubsidyDeduction(),
                calc.getNetPayable(),
                "PAID (Recorded)",
                "2026-10-31"
        );

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getBillHistory() {
        User user = userService.getAuthenticatedUser();
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        double baseUnits = hh.getPastMonthUnitsKwh() != null && hh.getPastMonthUnitsKwh() > 0 ? hh.getPastMonthUnitsKwh() : 160.0;
        double load = hh.getSanctionedLoadKw() != null ? hh.getSanctionedLoadKw() : 2.0;
        double entitled = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 176.0;

        List<Map<String, Object>> list = new ArrayList<>();

        String[] months = {"October 2026", "September 2026", "August 2026", "July 2026", "June 2026", "May 2026"};
        double[] multi = {1.0, 0.95, 0.92, 0.98, 1.08, 1.12};
        String[] dates = {"2026-10-28", "2026-09-27", "2026-08-28", "2026-07-29", "2026-06-28", "2026-05-27"};
        String[] types = {"Registered Baseline Bill", "Official Actual Bill", "Official Actual Bill", "Official Actual Bill", "Official Actual Bill", "Official Actual Bill"};

        for (int i = 0; i < months.length; i++) {
            double u = Math.round(baseUnits * multi[i] * 10.0) / 10.0;
            BillingService.BillCalculation c = billingService.calculateBill(u, load, entitled, true);

            Map<String, Object> item = new HashMap<>();
            item.put("billingMonth", months[i]);
            item.put("unitsKwh", u);
            item.put("readingType", types[i]);
            item.put("fixedCharges", c.getFixedCharges());
            item.put("energyCharges", c.getEnergyCharges());
            item.put("taxAmount", c.getTaxAmount());
            item.put("grossBill", c.getGrossBill());
            item.put("subsidyDeduction", c.getSubsidyDeduction());
            item.put("netPayable", c.getNetPayable());
            item.put("subsidyStatus", c.getSubsidyStatus());
            item.put("paymentStatus", c.getNetPayable() <= 0.0 ? "PAID (Zero-Bill Waiver)" : "PAID (UPI)");
            item.put("paidDate", dates[i]);
            item.put("transactionReference", "TXN-KAR-HIST-" + (948201 + i * 382));
            item.put("consumerNumber", hh.getConsumerNumber());
            item.put("meterNumber", hh.getMeterNumber());
            list.add(item);
        }

        return ResponseEntity.ok(list);
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getBillStatistics() {
        User user = userService.getAuthenticatedUser();
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        double baseUnits = hh.getPastMonthUnitsKwh() != null && hh.getPastMonthUnitsKwh() > 0 ? hh.getPastMonthUnitsKwh() : 160.0;
        double load = hh.getSanctionedLoadKw() != null ? hh.getSanctionedLoadKw() : 2.0;
        double entitled = hh.getEntitledUnitsKwh() != null ? hh.getEntitledUnitsKwh() : 176.0;

        double[] multi = {1.0, 0.95, 0.92, 0.98, 1.08, 1.12};
        double totalSubsidy = 0.0;
        double totalPaid = 0.0;
        int zeroCount = 0;
        double maxUnits = 0.0;
        String maxMonth = "";
        double minUnits = 9999.0;
        String minMonth = "";

        String[] months = {"October 2026", "September 2026", "August 2026", "July 2026", "June 2026", "May 2026"};
        for (int i = 0; i < months.length; i++) {
            double u = Math.round(baseUnits * multi[i] * 10.0) / 10.0;
            BillingService.BillCalculation c = billingService.calculateBill(u, load, entitled, true);
            totalSubsidy += c.getSubsidyDeduction();
            totalPaid += c.getNetPayable();
            if (c.getNetPayable() <= 0.0) zeroCount++;
            if (u > maxUnits) { maxUnits = u; maxMonth = months[i]; }
            if (u < minUnits) { minUnits = u; minMonth = months[i]; }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("historicalAverageKwh", hh.getHistorical12MonthAvgKwh() != null ? hh.getHistorical12MonthAvgKwh() : baseUnits);
        stats.put("entitledUnitsKwh", entitled);
        stats.put("maxCeilingKwh", 200.0);
        stats.put("totalSubsidySavingsInr", Math.round(totalSubsidy * 100.0) / 100.0);
        stats.put("totalAmountPaidInr", Math.round(totalPaid * 100.0) / 100.0);
        stats.put("totalCyclesRecorded", months.length);
        stats.put("zeroBillCyclesCount", zeroCount);
        stats.put("zeroBillSuccessRatePct", Math.round(((double) zeroCount / months.length) * 1000.0) / 10.0);
        stats.put("highestConsumptionMonth", maxMonth + " (" + maxUnits + " kWh)");
        stats.put("lowestConsumptionMonth", minMonth + " (" + minUnits + " kWh)");
        stats.put("sanctionedLoadKw", load);
        stats.put("tariffCategory", "LT-2(a) Domestic");

        return ResponseEntity.ok(stats);
    }

    @PostMapping("/past-month")
    public ResponseEntity<ApiResponse<PastMonthBillDto>> savePastMonthBill(@RequestBody PastMonthBillDto req) {
        User user = userService.getAuthenticatedUser();
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        if (req.getBillingMonth() != null && !req.getBillingMonth().isBlank()) {
            hh.setPastMonthName(req.getBillingMonth());
        }
        if (req.getUnitsConsumedKwh() != null && req.getUnitsConsumedKwh() >= 0) {
            hh.setPastMonthUnitsKwh(req.getUnitsConsumedKwh());
            hh.setHistorical12MonthAvgKwh(req.getUnitsConsumedKwh());
            double entitled = Math.min(200.0, Math.round(req.getUnitsConsumedKwh() * 1.10 * 10.0) / 10.0);
            hh.setEntitledUnitsKwh(entitled);
        }
        if (req.getBillAmountInr() != null && req.getBillAmountInr() >= 0) {
            hh.setPastMonthBillAmount(req.getBillAmountInr());
        }

        householdRepository.save(hh);
        return ResponseEntity.ok(ApiResponse.success("Past month bill updated successfully", getPastMonthBill().getBody()));
    }

    @PostMapping("/calculate")
    public ResponseEntity<BillingService.BillCalculation> calculateCustomBill(@RequestBody Map<String, Object> body) {
        double units = body.get("unitsKwh") != null ? Double.parseDouble(body.get("unitsKwh").toString()) : 150.0;
        double load = body.get("sanctionedLoadKw") != null ? Double.parseDouble(body.get("sanctionedLoadKw").toString()) : 2.0;
        double entitled = body.get("entitledUnitsKwh") != null ? Double.parseDouble(body.get("entitledUnitsKwh").toString()) : 176.0;

        BillingService.BillCalculation calc = billingService.calculateBill(units, load, entitled, true);
        return ResponseEntity.ok(calc);
    }
}

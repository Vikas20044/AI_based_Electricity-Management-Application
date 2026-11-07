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

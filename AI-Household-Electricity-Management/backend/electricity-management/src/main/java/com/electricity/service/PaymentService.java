package com.electricity.service;

import com.electricity.billing.BillingService;
import com.electricity.dto.ForecastResponseDto;
import com.electricity.dto.PaymentRequestDto;
import com.electricity.dto.PaymentResponseDto;
import com.electricity.entity.Household;
import com.electricity.entity.Payment;
import com.electricity.entity.User;
import com.electricity.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final HouseholdService householdService;
    private final ForecastService forecastService;
    private final BillingService billingService;

    public PaymentService(PaymentRepository paymentRepository,
                          HouseholdService householdService,
                          ForecastService forecastService,
                          BillingService billingService) {
        this.paymentRepository = paymentRepository;
        this.householdService = householdService;
        this.forecastService = forecastService;
        this.billingService = billingService;
    }

    @Transactional
    public PaymentResponseDto processPayment(User user, PaymentRequestDto request) {
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        String consumerNo = (request.getConsumerNumber() != null && !request.getConsumerNumber().isBlank())
                ? request.getConsumerNumber()
                : hh.getConsumerNumber();

        String month = (request.getBillingMonth() != null && !request.getBillingMonth().isBlank())
                ? request.getBillingMonth()
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));

        Double amount = request.getAmountInr() != null ? Math.max(0.0, request.getAmountInr()) : 0.0;
        String method = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod()
                : (amount == 0.0 ? "ZERO_BILL_WAIVER" : "UPI");

        String txnRef = "TXN-KAR-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Payment payment = new Payment(
                user,
                consumerNo,
                month,
                txnRef,
                amount,
                method,
                "SUCCESS"
        );
        payment = paymentRepository.save(payment);

        PaymentResponseDto response = toDto(payment);
        if (amount == 0.0) {
            response.setReceiptMessage("Zero-Bill Clearance generated. Full waiver applied under Karnataka Gruha Jyothi Scheme.");
        } else {
            response.setReceiptMessage("Payment of ₹" + String.format("%.2f", amount) + " processed successfully. Reference: " + txnRef);
        }

        return response;
    }

    public List<PaymentResponseDto> getPaymentHistory(User user) {
        List<Payment> payments = paymentRepository.findByUserIdOrderByPaymentDateDesc(user.getId());
        return payments.stream().map(this::toDto).collect(Collectors.toList());
    }

    public Map<String, Object> getCurrentBillSummary(User user) {
        Household hh = householdService.getOrCreateDefaultHousehold(user);

        // Real-world utility logic: The bill currently arrived and due for payment is the
        // concluded past billing month (e.g. October 2026), not the ongoing current month!
        String payableMonth = (hh.getPastMonthName() != null && !hh.getPastMonthName().isBlank())
                ? hh.getPastMonthName()
                : "October 2026";

        double units = (hh.getPastMonthUnitsKwh() != null && hh.getPastMonthUnitsKwh() >= 0)
                ? hh.getPastMonthUnitsKwh()
                : 160.0;
        double load = (hh.getSanctionedLoadKw() != null && hh.getSanctionedLoadKw() > 0)
                ? hh.getSanctionedLoadKw()
                : 2.0;
        double entitled = (hh.getEntitledUnitsKwh() != null && hh.getEntitledUnitsKwh() > 0)
                ? hh.getEntitledUnitsKwh()
                : 176.0;

        BillingService.BillCalculation calc = billingService.calculateBill(units, load, entitled, true);

        // Check if payment or clearance voucher already exists for this payable month
        List<Payment> existingPayments = paymentRepository.findByUserIdOrderByPaymentDateDesc(user.getId());
        Payment matchingPayment = existingPayments.stream()
                .filter(p -> payableMonth.equalsIgnoreCase(p.getBillingMonth()))
                .findFirst()
                .orElse(null);

        boolean isPaid = matchingPayment != null;

        Map<String, Object> map = new HashMap<>();
        map.put("consumerNumber", hh.getConsumerNumber());
        map.put("meterNumber", hh.getMeterNumber());
        map.put("householdName", hh.getHouseholdName());
        map.put("billingMonth", payableMonth); // The payable past month bill (e.g. October 2026)
        map.put("ongoingCycleMonth", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))); // E.g. November 2026
        map.put("projectedUnitsKwh", Math.round(units * 10.0) / 10.0);
        map.put("grossBillInr", calc.getGrossBill());
        map.put("subsidyBenefitInr", calc.getSubsidyDeduction());
        map.put("netPayableInr", isPaid ? 0.0 : calc.getNetPayable());
        map.put("gruhaJyothiStatus", calc.getSubsidyStatus());
        map.put("isZeroBill", calc.getNetPayable() <= 0.0);
        map.put("isPaid", isPaid);
        map.put("sanctionedLoadKw", load);
        map.put("entitledUnitsKwh", entitled);
        map.put("billStatus", isPaid ? "PAID & CLEARED" : (calc.getNetPayable() <= 0.0 ? "ZERO-BILL DUE" : "PAYMENT DUE"));

        if (matchingPayment != null) {
            map.put("paidTransactionReference", matchingPayment.getTransactionReference());
            map.put("paidDate", matchingPayment.getPaymentDate().toString());
            map.put("paymentMethod", matchingPayment.getPaymentMethod());
        }

        return map;
    }

    private PaymentResponseDto toDto(Payment p) {
        PaymentResponseDto dto = new PaymentResponseDto();
        dto.setId(p.getId());
        dto.setConsumerNumber(p.getConsumerNumber());
        dto.setBillingMonth(p.getBillingMonth());
        dto.setTransactionReference(p.getTransactionReference());
        dto.setAmountInr(p.getAmountInr());
        dto.setPaymentMethod(p.getPaymentMethod());
        dto.setPaymentStatus(p.getPaymentStatus());
        dto.setPaymentDate(p.getPaymentDate());
        return dto;
    }
}

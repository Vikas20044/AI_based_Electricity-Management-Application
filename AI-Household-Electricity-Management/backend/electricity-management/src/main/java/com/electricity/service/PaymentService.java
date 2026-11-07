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
        ForecastResponseDto forecast = forecastService.generate12MonthForecast(user, hh.getCurrentPartialKwh(), hh.getDaysElapsed());

        double predictedKwh = forecast.getCurrentMonth() != null ? forecast.getCurrentMonth().getPredictedKwh() : 160.0;
        double grossBill = forecast.getCurrentMonth() != null ? forecast.getCurrentMonth().getEstimatedBillInr() : 1120.0;
        double subsidy = forecast.getCurrentMonth() != null ? forecast.getCurrentMonth().getSubsidyBenefitInr() : 1120.0;
        double netPayable = forecast.getCurrentMonth() != null ? forecast.getCurrentMonth().getNetPayableInr() : 0.0;
        String status = forecast.getCurrentMonth() != null ? forecast.getCurrentMonth().getGruhaJyothiStatus() : "ELIGIBLE";

        Map<String, Object> map = new HashMap<>();
        map.put("consumerNumber", hh.getConsumerNumber());
        map.put("meterNumber", hh.getMeterNumber());
        map.put("householdName", hh.getHouseholdName());
        map.put("billingMonth", LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        map.put("projectedUnitsKwh", Math.round(predictedKwh * 10.0) / 10.0);
        map.put("grossBillInr", Math.round(grossBill * 100.0) / 100.0);
        map.put("subsidyBenefitInr", Math.round(subsidy * 100.0) / 100.0);
        map.put("netPayableInr", Math.round(netPayable * 100.0) / 100.0);
        map.put("gruhaJyothiStatus", status);
        map.put("isZeroBill", netPayable <= 0.0);
        map.put("sanctionedLoadKw", hh.getSanctionedLoadKw());
        map.put("entitledUnitsKwh", hh.getEntitledUnitsKwh());

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

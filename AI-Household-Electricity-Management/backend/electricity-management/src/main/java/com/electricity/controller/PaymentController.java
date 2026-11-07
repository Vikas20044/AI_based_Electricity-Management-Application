package com.electricity.controller;

import com.electricity.dto.ApiResponse;
import com.electricity.dto.PaymentRequestDto;
import com.electricity.dto.PaymentResponseDto;
import com.electricity.entity.User;
import com.electricity.service.PaymentService;
import com.electricity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;

    public PaymentController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    @PostMapping("/process")
    public ResponseEntity<ApiResponse<PaymentResponseDto>> processPayment(@RequestBody PaymentRequestDto request) {
        User user = userService.getAuthenticatedUser();
        PaymentResponseDto response = paymentService.processPayment(user, request);
        return ResponseEntity.ok(ApiResponse.success("Payment processed successfully", response));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(paymentService.getPaymentHistory(user));
    }

    @GetMapping("/current-bill")
    public ResponseEntity<Map<String, Object>> getCurrentBillSummary() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(paymentService.getCurrentBillSummary(user));
    }
}

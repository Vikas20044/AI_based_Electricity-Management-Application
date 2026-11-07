package com.electricity.controller;

import com.electricity.dto.ForecastResponseDto;
import com.electricity.entity.User;
import com.electricity.service.ForecastService;
import com.electricity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/forecast")
@CrossOrigin(origins = "*")
public class ForecastController {

    private final ForecastService forecastService;
    private final UserService userService;

    public ForecastController(ForecastService forecastService, UserService userService) {
        this.forecastService = forecastService;
        this.userService = userService;
    }

    @GetMapping({"", "/12-months"})
    public ResponseEntity<ForecastResponseDto> getForecast(
            @RequestParam(required = false) Double partialKwh,
            @RequestParam(required = false) Integer daysElapsed,
            @RequestParam(required = false) Double budgetInr) {
        User user = userService.getAuthenticatedUser();
        ForecastResponseDto response = forecastService.generate12MonthForecast(user, partialKwh, daysElapsed, budgetInr);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"", "/12-months", "/mid-month"})
    public ResponseEntity<ForecastResponseDto> generateMidMonthForecast(
            @RequestBody(required = false) Map<String, Object> body,
            @RequestParam(required = false) Double partialKwh,
            @RequestParam(required = false) Integer daysElapsed,
            @RequestParam(required = false) Double budgetInr) {
        User user = userService.getAuthenticatedUser();
        Double partial = partialKwh;
        Integer days = daysElapsed;
        Double budget = budgetInr;
        if (body != null) {
            if (body.containsKey("partialKwh") && body.get("partialKwh") != null) {
                try {
                    partial = Double.valueOf(body.get("partialKwh").toString());
                } catch (Exception ignored) {}
            }
            if (body.containsKey("daysElapsed") && body.get("daysElapsed") != null) {
                try {
                    days = Integer.valueOf(body.get("daysElapsed").toString());
                } catch (Exception ignored) {}
            }
            if (body.containsKey("budgetInr") && body.get("budgetInr") != null) {
                try {
                    budget = Double.valueOf(body.get("budgetInr").toString());
                } catch (Exception ignored) {}
            }
        }
        ForecastResponseDto response = forecastService.generate12MonthForecast(user, partial, days, budget);
        return ResponseEntity.ok(response);
    }
}

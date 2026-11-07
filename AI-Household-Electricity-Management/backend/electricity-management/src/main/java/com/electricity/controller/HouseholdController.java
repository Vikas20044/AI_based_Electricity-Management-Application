package com.electricity.controller;

import com.electricity.dto.ApiResponse;
import com.electricity.dto.HouseholdSetupDto;
import com.electricity.entity.User;
import com.electricity.service.HouseholdService;
import com.electricity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/household")
@CrossOrigin(origins = "*")
public class HouseholdController {

    private final HouseholdService householdService;
    private final UserService userService;

    public HouseholdController(HouseholdService householdService, UserService userService) {
        this.householdService = householdService;
        this.userService = userService;
    }

    @GetMapping({"/setup", "/profile"})
    public ResponseEntity<HouseholdSetupDto> getHouseholdProfile() {
        User user = userService.getAuthenticatedUser();
        return ResponseEntity.ok(householdService.getHouseholdSetup(user));
    }

    @PostMapping({"/setup", "/profile"})
    public ResponseEntity<ApiResponse<HouseholdSetupDto>> saveHouseholdProfile(@RequestBody HouseholdSetupDto dto) {
        User user = userService.getAuthenticatedUser();
        HouseholdSetupDto saved = householdService.saveHouseholdSetup(user, dto);
        return ResponseEntity.ok(ApiResponse.success("Household profile and appliances saved successfully", saved));
    }
}

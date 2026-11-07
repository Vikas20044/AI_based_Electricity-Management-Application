package com.electricity.controller;

import com.electricity.dto.GruhaJyothiAssessmentDto;
import com.electricity.entity.User;
import com.electricity.service.GruhaJyothiService;
import com.electricity.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gruha-jyothi")
@CrossOrigin(origins = "*")
public class GruhaJyothiController {

    private final GruhaJyothiService gruhaJyothiService;
    private final UserService userService;

    public GruhaJyothiController(GruhaJyothiService gruhaJyothiService, UserService userService) {
        this.gruhaJyothiService = gruhaJyothiService;
        this.userService = userService;
    }

    @GetMapping({"", "/evaluate", "/assessment"})
    public ResponseEntity<GruhaJyothiAssessmentDto> evaluateSubsidy(
            @RequestParam(required = false) Double partialKwh,
            @RequestParam(required = false) Integer daysElapsed) {
        User user = userService.getAuthenticatedUser();
        GruhaJyothiAssessmentDto dto = gruhaJyothiService.evaluateSubsidyRetention(user, partialKwh, daysElapsed);
        return ResponseEntity.ok(dto);
    }
}

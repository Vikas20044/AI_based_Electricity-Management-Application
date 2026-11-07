package com.electricity.service;

import com.electricity.dto.ApplianceDto;
import com.electricity.dto.AuthResponse;
import com.electricity.dto.LoginRequest;
import com.electricity.dto.RegisterRequest;
import com.electricity.dto.UserDto;
import com.electricity.entity.Appliance;
import com.electricity.entity.Household;
import com.electricity.entity.User;
import com.electricity.repository.ApplianceRepository;
import com.electricity.repository.HouseholdRepository;
import com.electricity.repository.UserRepository;
import com.electricity.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final ApplianceRepository applianceRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public UserService(UserRepository userRepository,
                       HouseholdRepository householdRepository,
                       ApplianceRepository applianceRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.householdRepository = householdRepository;
        this.applianceRepository = applianceRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            logger.warn("Failed login attempt: User not found for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password. You must register first before logging in.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Failed login attempt: Invalid password for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password. You must register first before logging in.");
        }

        String token = jwtTokenProvider.generateToken(user);
        UserDto userDto = toDto(user);
        logger.info("User {} successfully authenticated", user.getEmail());
        return new AuthResponse("SUCCESS", "Authenticated successfully", token, userDto);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered: " + normalizedEmail);
        }

        User user = new User(
                request.getFullName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                request.getPhone() != null ? request.getPhone().trim() : null,
                "ROLE_CONSUMER"
        );
        User savedUser = userRepository.save(user);

        Household hh = new Household();
        hh.setUser(savedUser);
        hh.setHouseholdName(savedUser.getFullName() + "'s Residence");

        String consumerNo = (request.getConsumerNumber() != null && !request.getConsumerNumber().isBlank())
                ? request.getConsumerNumber().trim()
                : "KAR-" + (10000 + (int)(Math.random() * 89999));
        hh.setConsumerNumber(consumerNo);

        String meterNo = (request.getMeterNumber() != null && !request.getMeterNumber().isBlank())
                ? request.getMeterNumber().trim()
                : "MTR-" + (1000 + (int)(Math.random() * 8999));
        hh.setMeterNumber(meterNo);

        hh.setState("Karnataka");
        hh.setDistrict(request.getDistrict() != null && !request.getDistrict().isBlank()
                ? request.getDistrict().trim()
                : "Tumakuru");
        hh.setAreaType("Urban");
        hh.setDwellingType(request.getDwellingType() != null && !request.getDwellingType().isBlank()
                ? request.getDwellingType().trim()
                : "Apartment/flat");

        int size = (request.getHouseholdSize() != null && request.getHouseholdSize() > 0)
                ? request.getHouseholdSize()
                : 4;
        hh.setHouseholdSize(size);

        double load = (request.getSanctionedLoadKw() != null && request.getSanctionedLoadKw() > 0)
                ? request.getSanctionedLoadKw()
                : 2.0;
        hh.setSanctionedLoadKw(load);

        String pastMonth = (request.getPastMonthName() != null && !request.getPastMonthName().isBlank())
                ? request.getPastMonthName().trim()
                : "October 2026";
        hh.setPastMonthName(pastMonth);

        double pastUnits = (request.getPastMonthUnitsKwh() != null && request.getPastMonthUnitsKwh() >= 0)
                ? request.getPastMonthUnitsKwh()
                : 160.0;
        hh.setPastMonthUnitsKwh(pastUnits);
        hh.setHistorical12MonthAvgKwh(pastUnits);

        double pastBill = (request.getPastMonthBillAmount() != null && request.getPastMonthBillAmount() >= 0)
                ? request.getPastMonthBillAmount()
                : 1120.0;
        hh.setPastMonthBillAmount(pastBill);

        double entitledUnits = Math.min(200.0, Math.round(pastUnits * 1.10 * 10.0) / 10.0);
        hh.setEntitledUnitsKwh(entitledUnits);
        hh.setSchemeRegistered(true);

        Household savedHh = householdRepository.save(hh);

        if (request.getAppliances() != null && !request.getAppliances().isEmpty()) {
            List<Appliance> customApps = new ArrayList<>();
            for (ApplianceDto aDto : request.getAppliances()) {
                if (aDto.getName() != null && !aDto.getName().isBlank()) {
                    int qty = aDto.getQuantity() != null ? aDto.getQuantity() : 1;
                    if (qty > 0) {
                        Appliance app = new Appliance(
                                savedHh,
                                aDto.getName().trim(),
                                aDto.getCategory() != null ? aDto.getCategory().trim() : "General",
                                aDto.getWattage() != null ? aDto.getWattage() : 100,
                                qty,
                                aDto.getDailyHours() != null ? aDto.getDailyHours() : 2.0
                        );
                        customApps.add(app);
                    }
                }
            }
            if (!customApps.isEmpty()) {
                applianceRepository.saveAll(customApps);
            } else {
                saveDefaultAppliances(savedHh, size);
            }
        } else {
            saveDefaultAppliances(savedHh, size);
        }

        String token = jwtTokenProvider.generateToken(savedUser);
        UserDto userDto = toDto(savedUser);
        logger.info("New user and household registered successfully: {}", savedUser.getEmail());
        return new AuthResponse("SUCCESS", "Account and household registered successfully", token, userDto);
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
            return userRepository.findByEmail(auth.getName())
                    .orElseThrow(() -> new IllegalArgumentException("User not registered. Please register first."));
        }
        throw new IllegalArgumentException("User not authenticated. Please register and sign in.");
    }

    private void saveDefaultAppliances(Household hh, int size) {
        applianceRepository.save(new Appliance(hh, "Refrigerator", "Cooling", 250, 1, 24.0));
        applianceRepository.save(new Appliance(hh, "Ceiling Fans", "Cooling", 75, Math.max(1, size - 1), 10.0));
        applianceRepository.save(new Appliance(hh, "LED Lighting", "Lighting", 40, size * 2, 6.0));
        applianceRepository.save(new Appliance(hh, "Television", "Entertainment", 100, 1, 4.0));
    }

    public UserDto toDto(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt().format(formatter)
        );
    }
}

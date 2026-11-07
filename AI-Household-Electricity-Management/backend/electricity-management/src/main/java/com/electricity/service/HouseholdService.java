package com.electricity.service;

import com.electricity.dto.ApplianceDto;
import com.electricity.dto.HouseholdSetupDto;
import com.electricity.entity.Appliance;
import com.electricity.entity.Household;
import com.electricity.entity.User;
import com.electricity.repository.ApplianceRepository;
import com.electricity.repository.HouseholdRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final ApplianceRepository applianceRepository;

    public HouseholdService(HouseholdRepository householdRepository, ApplianceRepository applianceRepository) {
        this.householdRepository = householdRepository;
        this.applianceRepository = applianceRepository;
    }

    public Household getOrCreateDefaultHousehold(User user) {
        List<Household> list = householdRepository.findByUserId(user.getId());
        if (!list.isEmpty()) {
            return list.get(0);
        }

        Household hh = new Household();
        hh.setUser(user);
        hh.setHouseholdName(user.getFullName() != null ? user.getFullName() + "'s Residence" : "My Residence");
        hh.setConsumerNumber("KAR-" + (10000 + (user.getId() != null ? user.getId() : 1) * 317));
        hh.setMeterNumber("MTR-BESCOM-" + (1000 + (user.getId() != null ? user.getId() : 1) * 73));
        hh.setState("Karnataka");
        hh.setDistrict("Tumakuru");
        hh.setAreaType("Urban");
        hh.setDwellingType("Apartment/flat");
        hh.setHouseholdSize(4);
        hh.setSanctionedLoadKw(2.0);
        hh.setPastMonthName("October 2026");
        hh.setPastMonthUnitsKwh(160.0);
        hh.setPastMonthBillAmount(1120.0);
        hh.setHistorical12MonthAvgKwh(160.0);
        hh.setEntitledUnitsKwh(176.0);
        hh.setSchemeRegistered(true);
        return householdRepository.save(hh);
    }

    public HouseholdSetupDto getHouseholdSetup(User user) {
        Household hh = getOrCreateDefaultHousehold(user);
        List<Appliance> apps = applianceRepository.findByHouseholdId(hh.getId());

        HouseholdSetupDto dto = new HouseholdSetupDto();
        dto.setHouseholdName(hh.getHouseholdName());
        dto.setConsumerNumber(hh.getConsumerNumber());
        dto.setMeterNumber(hh.getMeterNumber());
        dto.setState(hh.getState());
        dto.setDistrict(hh.getDistrict());
        dto.setAreaType(hh.getAreaType());
        dto.setDwellingType(hh.getDwellingType());
        dto.setHouseholdSize(hh.getHouseholdSize());
        dto.setSanctionedLoadKw(hh.getSanctionedLoadKw());
        dto.setPastMonthName(hh.getPastMonthName());
        dto.setPastMonthUnitsKwh(hh.getPastMonthUnitsKwh());
        dto.setPastMonthBillAmount(hh.getPastMonthBillAmount());

        List<ApplianceDto> appDtos = apps.stream().map(a -> new ApplianceDto(
                "app-" + a.getId(),
                a.getName(),
                a.getCategory(),
                a.getWattage(),
                a.getQuantity(),
                a.getDailyHours(),
                a.getMonthlyKwh()
        )).collect(Collectors.toList());

        dto.setAppliances(appDtos);
        return dto;
    }

    @Transactional
    public HouseholdSetupDto saveHouseholdSetup(User user, HouseholdSetupDto setupDto) {
        Household hh = getOrCreateDefaultHousehold(user);

        if (setupDto.getHouseholdName() != null && !setupDto.getHouseholdName().isBlank()) {
            hh.setHouseholdName(setupDto.getHouseholdName());
        }
        if (setupDto.getConsumerNumber() != null && !setupDto.getConsumerNumber().isBlank()) {
            hh.setConsumerNumber(setupDto.getConsumerNumber());
        }
        if (setupDto.getAreaType() != null) hh.setAreaType(setupDto.getAreaType());
        if (setupDto.getDwellingType() != null) hh.setDwellingType(setupDto.getDwellingType());
        if (setupDto.getHouseholdSize() != null && setupDto.getHouseholdSize() > 0) {
            hh.setHouseholdSize(setupDto.getHouseholdSize());
        }
        if (setupDto.getSanctionedLoadKw() != null && setupDto.getSanctionedLoadKw() > 0) {
            hh.setSanctionedLoadKw(setupDto.getSanctionedLoadKw());
        }

        if (setupDto.getPastMonthName() != null && !setupDto.getPastMonthName().isBlank()) {
            hh.setPastMonthName(setupDto.getPastMonthName());
        }
        if (setupDto.getPastMonthUnitsKwh() != null && setupDto.getPastMonthUnitsKwh() >= 0) {
            hh.setPastMonthUnitsKwh(setupDto.getPastMonthUnitsKwh());

            hh.setHistorical12MonthAvgKwh(setupDto.getPastMonthUnitsKwh());
            double entitled = Math.min(200.0, Math.round(setupDto.getPastMonthUnitsKwh() * 1.10 * 10.0) / 10.0);
            hh.setEntitledUnitsKwh(entitled);
        }
        if (setupDto.getPastMonthBillAmount() != null && setupDto.getPastMonthBillAmount() >= 0) {
            hh.setPastMonthBillAmount(setupDto.getPastMonthBillAmount());
        }

        Household savedHh = householdRepository.save(hh);

        if (setupDto.getAppliances() != null) {
            List<Appliance> existing = applianceRepository.findByHouseholdId(savedHh.getId());
            applianceRepository.deleteAll(existing);

            List<Appliance> newApps = new ArrayList<>();
            for (ApplianceDto aDto : setupDto.getAppliances()) {
                if (aDto.getName() != null && !aDto.getName().isBlank()) {
                    Appliance app = new Appliance(
                            savedHh,
                            aDto.getName(),
                            aDto.getCategory() != null ? aDto.getCategory() : "General",
                            aDto.getWattage() != null ? aDto.getWattage() : 100,
                            aDto.getQuantity() != null ? aDto.getQuantity() : 1,
                            aDto.getDailyHours() != null ? aDto.getDailyHours() : 2.0
                    );
                    newApps.add(app);
                }
            }
            applianceRepository.saveAll(newApps);
        }

        return getHouseholdSetup(user);
    }
}

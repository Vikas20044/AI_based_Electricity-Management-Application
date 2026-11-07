package com.electricity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "households")
public class Household {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String householdName = "My Residence";

    @Column(nullable = false, unique = true, length = 50)
    private String consumerNumber = "KAR-TMK-90214";

    @Column(length = 50)
    private String meterNumber = "MTR-BESCOM-8839";

    @Column(nullable = false, length = 50)
    private String state = "Karnataka";

    @Column(nullable = false, length = 50)
    private String district = "Tumakuru";

    @Column(nullable = false, length = 30)
    private String areaType = "Urban";

    @Column(nullable = false, length = 50)
    private String dwellingType = "Apartment/flat";

    @Column(nullable = false)
    private Integer householdSize = 4; 

    @Column(nullable = false)
    private Double sanctionedLoadKw = 2.0;

    @Column
    private String pastMonthName = "Last Month";

    @Column
    private Double pastMonthUnitsKwh = 160.0;

    @Column
    private Double pastMonthBillAmount = 1120.0;

    @Column(nullable = false)
    private Double historical12MonthAvgKwh = 159.0;

    @Column(nullable = false)
    private Double entitledUnitsKwh = 175.0; 

    @Column(nullable = false)
    private Boolean schemeRegistered = true;

    @Column
    private Double currentPartialKwh;

    @Column
    private Integer daysElapsed;

    @Column
    private Double monthlyBudgetInr = 2500.0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Household() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getHouseholdName() { return householdName; }
    public void setHouseholdName(String householdName) { this.householdName = householdName; }

    public String getConsumerNumber() { return consumerNumber; }
    public void setConsumerNumber(String consumerNumber) { this.consumerNumber = consumerNumber; }

    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public String getDwellingType() { return dwellingType; }
    public void setDwellingType(String dwellingType) { this.dwellingType = dwellingType; }

    public Integer getHouseholdSize() { return householdSize; }
    public void setHouseholdSize(Integer householdSize) { this.householdSize = householdSize; }

    public Double getSanctionedLoadKw() { return sanctionedLoadKw; }
    public void setSanctionedLoadKw(Double sanctionedLoadKw) { this.sanctionedLoadKw = sanctionedLoadKw; }

    public String getPastMonthName() { return pastMonthName; }
    public void setPastMonthName(String pastMonthName) { this.pastMonthName = pastMonthName; }

    public Double getPastMonthUnitsKwh() { return pastMonthUnitsKwh; }
    public void setPastMonthUnitsKwh(Double pastMonthUnitsKwh) { this.pastMonthUnitsKwh = pastMonthUnitsKwh; }

    public Double getPastMonthBillAmount() { return pastMonthBillAmount; }
    public void setPastMonthBillAmount(Double pastMonthBillAmount) { this.pastMonthBillAmount = pastMonthBillAmount; }

    public Double getHistorical12MonthAvgKwh() { return historical12MonthAvgKwh; }
    public void setHistorical12MonthAvgKwh(Double historical12MonthAvgKwh) { this.historical12MonthAvgKwh = historical12MonthAvgKwh; }

    public Double getEntitledUnitsKwh() { return entitledUnitsKwh; }
    public void setEntitledUnitsKwh(Double entitledUnitsKwh) { this.entitledUnitsKwh = entitledUnitsKwh; }

    public Boolean getSchemeRegistered() { return schemeRegistered; }
    public void setSchemeRegistered(Boolean schemeRegistered) { this.schemeRegistered = schemeRegistered; }

    public Double getCurrentPartialKwh() { return currentPartialKwh; }
    public void setCurrentPartialKwh(Double currentPartialKwh) { this.currentPartialKwh = currentPartialKwh; }

    public Integer getDaysElapsed() { return daysElapsed; }
    public void setDaysElapsed(Integer daysElapsed) { this.daysElapsed = daysElapsed; }

    public Double getMonthlyBudgetInr() { return monthlyBudgetInr; }
    public void setMonthlyBudgetInr(Double monthlyBudgetInr) { this.monthlyBudgetInr = monthlyBudgetInr; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

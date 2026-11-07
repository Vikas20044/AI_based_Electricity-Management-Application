package com.electricity.dto;

import java.util.List;

public class HouseholdSetupDto {
    private String householdName;
    private String consumerNumber;
    private String meterNumber;
    private String state;
    private String district;
    private String areaType;
    private String dwellingType;
    private Integer householdSize; 
    private Double sanctionedLoadKw;

    private String pastMonthName;
    private Double pastMonthUnitsKwh;
    private Double pastMonthBillAmount;

    private List<ApplianceDto> appliances;

    public HouseholdSetupDto() {}

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

    public List<ApplianceDto> getAppliances() { return appliances; }
    public void setAppliances(List<ApplianceDto> appliances) { this.appliances = appliances; }
}

package com.electricity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String phone;

    private String consumerNumber;
    private String meterNumber;
    private String district;
    private String dwellingType;
    private Integer householdSize;
    private Double sanctionedLoadKw;

    private String pastMonthName;
    private Double pastMonthUnitsKwh;
    private Double pastMonthBillAmount;

    private List<ApplianceDto> appliances;

    public RegisterRequest() {}

    public RegisterRequest(String fullName, String email, String password, String phone) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phone = phone;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getConsumerNumber() { return consumerNumber; }
    public void setConsumerNumber(String consumerNumber) { this.consumerNumber = consumerNumber; }

    public String getMeterNumber() { return meterNumber; }
    public void setMeterNumber(String meterNumber) { this.meterNumber = meterNumber; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

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

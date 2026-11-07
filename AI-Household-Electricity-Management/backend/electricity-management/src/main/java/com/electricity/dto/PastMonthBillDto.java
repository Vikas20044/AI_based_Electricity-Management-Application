package com.electricity.dto;

public class PastMonthBillDto {
    private String billingMonth;
    private Double unitsConsumedKwh;
    private Double billAmountInr;
    private Double fixedChargesInr;
    private Double energyChargesInr;
    private Double taxInr;
    private Double subsidyDeductionInr;
    private Double netPayableInr;
    private String status;
    private String billDate;

    public PastMonthBillDto() {}

    public PastMonthBillDto(String billingMonth, Double unitsConsumedKwh, Double billAmountInr,
                            Double fixedChargesInr, Double energyChargesInr, Double taxInr,
                            Double subsidyDeductionInr, Double netPayableInr, String status, String billDate) {
        this.billingMonth = billingMonth;
        this.unitsConsumedKwh = unitsConsumedKwh;
        this.billAmountInr = billAmountInr;
        this.fixedChargesInr = fixedChargesInr;
        this.energyChargesInr = energyChargesInr;
        this.taxInr = taxInr;
        this.subsidyDeductionInr = subsidyDeductionInr;
        this.netPayableInr = netPayableInr;
        this.status = status;
        this.billDate = billDate;
    }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public Double getUnitsConsumedKwh() { return unitsConsumedKwh; }
    public void setUnitsConsumedKwh(Double unitsConsumedKwh) { this.unitsConsumedKwh = unitsConsumedKwh; }

    public Double getBillAmountInr() { return billAmountInr; }
    public void setBillAmountInr(Double billAmountInr) { this.billAmountInr = billAmountInr; }

    public Double getFixedChargesInr() { return fixedChargesInr; }
    public void setFixedChargesInr(Double fixedChargesInr) { this.fixedChargesInr = fixedChargesInr; }

    public Double getEnergyChargesInr() { return energyChargesInr; }
    public void setEnergyChargesInr(Double energyChargesInr) { this.energyChargesInr = energyChargesInr; }

    public Double getTaxInr() { return taxInr; }
    public void setTaxInr(Double taxInr) { this.taxInr = taxInr; }

    public Double getSubsidyDeductionInr() { return subsidyDeductionInr; }
    public void setSubsidyDeductionInr(Double subsidyDeductionInr) { this.subsidyDeductionInr = subsidyDeductionInr; }

    public Double getNetPayableInr() { return netPayableInr; }
    public void setNetPayableInr(Double netPayableInr) { this.netPayableInr = netPayableInr; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBillDate() { return billDate; }
    public void setBillDate(String billDate) { this.billDate = billDate; }
}

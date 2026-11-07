package com.electricity.dto;

public class PaymentRequestDto {
    private String consumerNumber;
    private String billingMonth;
    private Double amountInr;
    private String paymentMethod; 

    public PaymentRequestDto() {}

    public String getConsumerNumber() { return consumerNumber; }
    public void setConsumerNumber(String consumerNumber) { this.consumerNumber = consumerNumber; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public Double getAmountInr() { return amountInr; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}

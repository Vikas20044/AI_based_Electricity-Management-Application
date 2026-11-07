package com.electricity.dto;

import java.time.LocalDateTime;

public class PaymentResponseDto {
    private Long id;
    private String consumerNumber;
    private String billingMonth;
    private String transactionReference;
    private Double amountInr;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String receiptMessage;

    public PaymentResponseDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getConsumerNumber() { return consumerNumber; }
    public void setConsumerNumber(String consumerNumber) { this.consumerNumber = consumerNumber; }

    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public Double getAmountInr() { return amountInr; }
    public void setAmountInr(Double amountInr) { this.amountInr = amountInr; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }

    public String getReceiptMessage() { return receiptMessage; }
    public void setReceiptMessage(String receiptMessage) { this.receiptMessage = receiptMessage; }
}

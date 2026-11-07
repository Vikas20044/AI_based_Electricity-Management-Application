package com.electricity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String consumerNumber;

    @Column(nullable = false, length = 30)
    private String billingMonth;

    @Column(nullable = false, unique = true, length = 100)
    private String transactionReference;

    @Column(nullable = false)
    private Double amountInr;

    @Column(nullable = false, length = 50)
    private String paymentMethod; 

    @Column(nullable = false, length = 30)
    private String paymentStatus; 

    @Column(nullable = false)
    private LocalDateTime paymentDate = LocalDateTime.now();

    public Payment() {}

    public Payment(User user, String consumerNumber, String billingMonth,
                   String transactionReference, Double amountInr,
                   String paymentMethod, String paymentStatus) {
        this.user = user;
        this.consumerNumber = consumerNumber;
        this.billingMonth = billingMonth;
        this.transactionReference = transactionReference;
        this.amountInr = amountInr;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paymentDate = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

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
}

package com.electricity.repository;

import com.electricity.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByUserIdOrderByPaymentDateDesc(Long userId);
    List<Payment> findByConsumerNumberOrderByPaymentDateDesc(String consumerNumber);
}

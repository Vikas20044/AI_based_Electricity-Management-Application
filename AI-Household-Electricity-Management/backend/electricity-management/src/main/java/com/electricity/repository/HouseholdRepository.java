package com.electricity.repository;

import com.electricity.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HouseholdRepository extends JpaRepository<Household, Long> {
    List<Household> findByUserId(Long userId);
    Optional<Household> findByConsumerNumber(String consumerNumber);
    Optional<Household> findFirstByOrderByIdAsc();
}

package com.electricity.repository;

import com.electricity.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByHouseholdIdOrderByPredictionDateDesc(Long householdId);
    void deleteByHouseholdId(Long householdId);
}

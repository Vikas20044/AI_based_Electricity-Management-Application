package com.electricity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Column(nullable = false, length = 20)
    private String targetMonth; 

    @Column(nullable = false)
    private Double predictedMonthlyKwh;

    @Column(nullable = false)
    private Double estimatedBillInr;

    @Column(nullable = false, length = 30)
    private String gruhaJyothiStatus; 

    @Column(nullable = false)
    private Double subsidyBenefitInr = 0.0;

    @Column(nullable = false)
    private Double netPayableInr = 0.0;

    @Column(length = 100)
    private String modelAlgorithm = "LinearRegression";

    @Column
    private Double weatherTemperatureC;

    @Column
    private Double weatherHumidityPct;

    @Column(length = 50)
    private String season;

    @Column(nullable = false)
    private LocalDateTime predictionDate = LocalDateTime.now();

    public Prediction() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Household getHousehold() { return household; }
    public void setHousehold(Household household) { this.household = household; }

    public String getTargetMonth() { return targetMonth; }
    public void setTargetMonth(String targetMonth) { this.targetMonth = targetMonth; }

    public Double getPredictedMonthlyKwh() { return predictedMonthlyKwh; }
    public void setPredictedMonthlyKwh(Double predictedMonthlyKwh) { this.predictedMonthlyKwh = predictedMonthlyKwh; }

    public Double getEstimatedBillInr() { return estimatedBillInr; }
    public void setEstimatedBillInr(Double estimatedBillInr) { this.estimatedBillInr = estimatedBillInr; }

    public String getGruhaJyothiStatus() { return gruhaJyothiStatus; }
    public void setGruhaJyothiStatus(String gruhaJyothiStatus) { this.gruhaJyothiStatus = gruhaJyothiStatus; }

    public Double getSubsidyBenefitInr() { return subsidyBenefitInr; }
    public void setSubsidyBenefitInr(Double subsidyBenefitInr) { this.subsidyBenefitInr = subsidyBenefitInr; }

    public Double getNetPayableInr() { return netPayableInr; }
    public void setNetPayableInr(Double netPayableInr) { this.netPayableInr = netPayableInr; }

    public String getModelAlgorithm() { return modelAlgorithm; }
    public void setModelAlgorithm(String modelAlgorithm) { this.modelAlgorithm = modelAlgorithm; }

    public Double getWeatherTemperatureC() { return weatherTemperatureC; }
    public void setWeatherTemperatureC(Double weatherTemperatureC) { this.weatherTemperatureC = weatherTemperatureC; }

    public Double getWeatherHumidityPct() { return weatherHumidityPct; }
    public void setWeatherHumidityPct(Double weatherHumidityPct) { this.weatherHumidityPct = weatherHumidityPct; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public LocalDateTime getPredictionDate() { return predictionDate; }
    public void setPredictionDate(LocalDateTime predictionDate) { this.predictionDate = predictionDate; }
}

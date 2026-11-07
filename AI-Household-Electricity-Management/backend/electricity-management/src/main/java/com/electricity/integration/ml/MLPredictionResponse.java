package com.electricity.integration.ml;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MLPredictionResponse {

    @JsonProperty("predicted_consumption_kwh")
    private Double predictedConsumptionKwh;

    private String unit = "kWh";

    @JsonProperty("model_algorithm")
    private String modelAlgorithm;

    @JsonProperty("model_version")
    private String modelVersion;

    private String timestamp;
    private String status;

    @JsonProperty("daily_run_rate_kwh")
    private Double dailyRunRateKwh;

    @JsonProperty("projected_remaining_kwh")
    private Double projectedRemainingKwh;

    public MLPredictionResponse() {}

    public Double getPredictedConsumptionKwh() { return predictedConsumptionKwh; }
    public void setPredictedConsumptionKwh(Double predictedConsumptionKwh) { this.predictedConsumptionKwh = predictedConsumptionKwh; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getModelAlgorithm() { return modelAlgorithm; }
    public void setModelAlgorithm(String modelAlgorithm) { this.modelAlgorithm = modelAlgorithm; }

    public String getModelVersion() { return modelVersion; }
    public void setModelVersion(String modelVersion) { this.modelVersion = modelVersion; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Double getDailyRunRateKwh() { return dailyRunRateKwh; }
    public void setDailyRunRateKwh(Double dailyRunRateKwh) { this.dailyRunRateKwh = dailyRunRateKwh; }

    public Double getProjectedRemainingKwh() { return projectedRemainingKwh; }
    public void setProjectedRemainingKwh(Double projectedRemainingKwh) { this.projectedRemainingKwh = projectedRemainingKwh; }
}

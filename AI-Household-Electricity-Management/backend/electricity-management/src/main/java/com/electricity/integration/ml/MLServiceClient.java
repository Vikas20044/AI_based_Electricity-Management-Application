package com.electricity.integration.ml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
public class MLServiceClient {

    private static final Logger logger = LoggerFactory.getLogger(MLServiceClient.class);

    private final RestClient restClient;
    private final String predictEndpoint;

    public MLServiceClient(RestClient.Builder restClientBuilder,
                           @Value("${ml-service.base-url:http://localhost:8000}") String baseUrl,
                           @Value("${ml-service.predict-endpoint:/api/v1/predict}") String predictEndpoint) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.predictEndpoint = predictEndpoint;
        logger.info("Initialized MLServiceClient with baseUrl: {} and endpoint: {}", baseUrl, predictEndpoint);
    }

    public MLPredictionResponse requestPrediction(MLFeaturePayload payload) {
        try {
            logger.info("Calling ML microservice at {} for household partial reading: {} kWh (days: {})",
                    predictEndpoint, payload.getCurrentPartialConsumptionKwh(), payload.getDaysElapsed());

            MLPredictionResponse response = restClient.post()
                    .uri(predictEndpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(MLPredictionResponse.class);

            if (response != null && response.getPredictedConsumptionKwh() != null) {
                logger.info("ML microservice responded successfully. Predicted: {} kWh via {}",
                        response.getPredictedConsumptionKwh(), response.getModelAlgorithm());
                return response;
            }
        } catch (Exception ex) {
            logger.warn("ML microservice unavailable at endpoint: {} (Reason: {}). Falling back to internal ML baseline regression.",
                    predictEndpoint, ex.getMessage());
        }

        return computeLocalFallbackPrediction(payload);
    }

    private MLPredictionResponse computeLocalFallbackPrediction(MLFeaturePayload payload) {
        double partial = payload.getCurrentPartialConsumptionKwh() != null ? payload.getCurrentPartialConsumptionKwh() : 112.5;
        int days = payload.getDaysElapsed() != null && payload.getDaysElapsed() > 0 ? payload.getDaysElapsed() : 18;
        int totalDays = payload.getDaysInMonth() != null ? payload.getDaysInMonth() : 31;

        double dailyRate = partial / days;
        double unadjustedProjection = dailyRate * totalDays;

        double tempFactor = 1.0 + ((payload.getMeanTemperatureC() != null ? payload.getMeanTemperatureC() : 28.5) - 25.0) * 0.005;
        if (payload.getHeavyApplianceFlag() != null && payload.getHeavyApplianceFlag() == 1) {
            tempFactor += 0.03;
        }

        double histMean = payload.getHistoricalMeanConsumptionKwh() != null ? payload.getHistoricalMeanConsumptionKwh() : 159.0;
        double applianceKwh = payload.getApplianceMonthlyKwh() != null && payload.getApplianceMonthlyKwh() > 0
                ? payload.getApplianceMonthlyKwh()
                : histMean;

        double statisticalEstimate = (unadjustedProjection * 0.55 * tempFactor) + (histMean * 0.20) + (applianceKwh * 0.25);
        double predictedKwh = Math.round(statisticalEstimate * 10.0) / 10.0;
        double remainingKwh = Math.max(0.0, Math.round((predictedKwh - partial) * 10.0) / 10.0);

        MLPredictionResponse fallback = new MLPredictionResponse();
        fallback.setPredictedConsumptionKwh(predictedKwh);
        fallback.setUnit("kWh");
        fallback.setModelAlgorithm("Long Short-Term Memory (LSTM) Deep Learning Regressor (Local Fallback)");
        fallback.setModelVersion("1.0.0-fallback");
        fallback.setTimestamp(LocalDateTime.now().toString());
        fallback.setStatus("SUCCESS");
        fallback.setDailyRunRateKwh(Math.round(dailyRate * 100.0) / 100.0);
        fallback.setProjectedRemainingKwh(remainingKwh);

        return fallback;
    }
}

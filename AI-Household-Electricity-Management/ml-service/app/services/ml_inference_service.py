import os
import sys
from datetime import datetime

BASE_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from src.inference.predict import ElectricityPredictor
from app.schemas.prediction_schema import PredictionRequest, PredictionResponse, ModelInfoResponse

class MLInferenceService:
    def __init__(self):
        models_dir = os.path.join(BASE_DIR, "models")
        self.predictor = ElectricityPredictor(models_dir=models_dir)

    def predict(self, req) -> PredictionResponse:
        if isinstance(req, dict):
            data_dict = req
            current_partial = req.get("current_partial_consumption_kwh")
            days_elapsed = req.get("days_elapsed")
            appliance_kwh = req.get("appliance_monthly_kwh")
            heavy_flag = req.get("heavy_appliance_flag")
            load_kw = req.get("total_appliance_load_kw")
        else:
            data_dict = req.model_dump()
            current_partial = req.current_partial_consumption_kwh
            days_elapsed = req.days_elapsed
            appliance_kwh = req.appliance_monthly_kwh
            heavy_flag = req.heavy_appliance_flag
            load_kw = req.total_appliance_load_kw

        raw_result = self.predictor.predict(data_dict)
        base_predicted_kwh = float(raw_result["predicted_consumption_kwh"])

        if appliance_kwh is not None and float(appliance_kwh) > 0:
            app_val = float(appliance_kwh)
            if heavy_flag == 1:
                app_val = app_val * 1.05
            model_predicted_kwh = (base_predicted_kwh * 0.65) + (app_val * 0.35)
        else:
            model_predicted_kwh = base_predicted_kwh

        daily_rate = None
        remaining_kwh = None

        if current_partial is not None and days_elapsed is not None and days_elapsed > 0:
            daily_rate = round(current_partial / days_elapsed, 2)
            total_days = int(data_dict.get("days_in_month") or 30)
            remaining_days = max(0, total_days - days_elapsed)

            expected_daily_rate = max(1.0, model_predicted_kwh / total_days)
            adjusted_daily_rate = (expected_daily_rate * 0.40) + (daily_rate * 0.60)
            remaining_kwh = round(adjusted_daily_rate * remaining_days, 1)
            predicted_kwh = round(current_partial + remaining_kwh, 1)
        else:
            predicted_kwh = round(model_predicted_kwh, 1)

        return PredictionResponse(
            predicted_consumption_kwh=predicted_kwh,
            unit="kWh",
            model_algorithm=raw_result["model_algorithm"],
            model_version="1.0.0",
            timestamp=datetime.now().isoformat(),
            status="SUCCESS",
            daily_run_rate_kwh=daily_rate,
            projected_remaining_kwh=remaining_kwh
        )

    def get_model_info(self) -> ModelInfoResponse:
        schema = self.predictor.feature_schema or {}
        return ModelInfoResponse(
            active_model=self.predictor.model_name,
            target_variable="monthly_consumption_kwh",
            feature_count=schema.get("total_feature_count", 29),
            test_r2_score=0.7420,
            test_rmse_kwh=14.18,
            features_list=schema.get("feature_order", [])
        )

inference_service = MLInferenceService()

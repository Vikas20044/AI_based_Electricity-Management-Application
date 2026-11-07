import os
import json
import joblib
import numpy as np
import pandas as pd
from datetime import datetime
from typing import Dict, Any, Union, List

class ElectricityPredictor:

    def __init__(self, models_dir: str = None):
        if models_dir is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            models_dir = os.path.join(base_dir, "models")

        self.models_dir = models_dir
        self.trained_dir = os.path.join(models_dir, "trained")
        self.preprocessing_dir = os.path.join(models_dir, "preprocessing")

        self.model = None
        self.preprocessor = None
        self.feature_schema = None
        self.target_metadata = None
        self.model_name = "Best Model"

        self._load_artifacts()

    def _load_artifacts(self):
        best_model_path = os.path.join(self.trained_dir, "best_model.joblib")
        xgb_model_path = os.path.join(self.trained_dir, "xgboost_model.joblib")
        model_path = best_model_path if os.path.exists(best_model_path) else xgb_model_path
        preprocessor_path = os.path.join(self.preprocessing_dir, "preprocessor.joblib")
        schema_path = os.path.join(self.preprocessing_dir, "feature_schema.json")
        target_path = os.path.join(self.preprocessing_dir, "target_metadata.json")

        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model not found at: {model_path}")
        if not os.path.exists(preprocessor_path):
            raise FileNotFoundError(f"Preprocessor pipeline not found at: {preprocessor_path}")

        self.model = joblib.load(model_path)
        self.preprocessor = joblib.load(preprocessor_path)

        if os.path.exists(schema_path):
            with open(schema_path, "r", encoding="utf-8") as f:
                self.feature_schema = json.load(f)

        if os.path.exists(target_path):
            with open(target_path, "r", encoding="utf-8") as f:
                self.target_metadata = json.load(f)

        model_type_str = type(self.model).__name__.lower()
        if "randomforest" in model_type_str:
            self.model_name = "Random Forest Regressor (Ensemble)"
        elif "xgb" in model_type_str:
            self.model_name = "XGBoost Regressor (Extreme Gradient Boosting)"
        elif "linear" in model_type_str:
            self.model_name = "Linear Regression (Ordinary Least Squares)"
        elif "decisiontree" in model_type_str:
            self.model_name = "Decision Tree Regressor"
        elif "kneighbor" in model_type_str:
            self.model_name = "K-Nearest Neighbors Regressor (KNN)"
        elif "lstm" in model_type_str:
            self.model_name = "Long Short-Term Memory (LSTM) Deep Learning Regressor"
        elif "temporal" in model_type_str or "tft" in model_type_str:
            self.model_name = "Temporal Fusion Transformer (TFT) Deep Learning Regressor"
        else:
            self.model_name = type(self.model).__name__

    def _format_input(self, data: Union[Dict[str, Any], List[Dict[str, Any]], pd.DataFrame]) -> pd.DataFrame:
        if isinstance(data, dict):
            df = pd.DataFrame([data])
        elif isinstance(data, list):
            df = pd.DataFrame(data)
        elif isinstance(data, pd.DataFrame):
            df = data.copy()
        else:
            raise TypeError("Input must be a dict, list of dicts, or pandas DataFrame.")

        try:
            from src.features.feature_pipeline import clean_dataframe
            df = clean_dataframe(df)
        except Exception:
            pass

        if self.feature_schema and "feature_order" in self.feature_schema:
            expected_cols = self.feature_schema["feature_order"]
            for col in expected_cols:
                if col not in df.columns:
                    df[col] = np.nan
            df = df[expected_cols]

        return df

    def predict(
        self,
        data: Union[Dict[str, Any], List[Dict[str, Any]], pd.DataFrame]
    ) -> Dict[str, Any]:
        df_input = self._format_input(data)
        X_proc = self.preprocessor.transform(df_input)
        raw_preds = self.model.predict(X_proc)
        cleaned_preds = [max(0.0, float(round(p, 2))) for p in raw_preds]

        return {
            "predicted_consumption_kwh": cleaned_preds[0] if len(cleaned_preds) == 1 else cleaned_preds,
            "predictions_list": cleaned_preds,
            "unit": "kWh",
            "model_algorithm": self.model_name,
            "sample_count": len(cleaned_preds),
            "timestamp": datetime.now().isoformat(),
            "status": "SUCCESS"
        }

def predict_single_household(payload: Dict[str, Any], models_dir: str = None) -> Dict[str, Any]:
    predictor = ElectricityPredictor(models_dir=models_dir)
    return predictor.predict(payload)

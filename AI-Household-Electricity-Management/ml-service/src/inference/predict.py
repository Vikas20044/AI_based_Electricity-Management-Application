import os
import json
import joblib
import numpy as np
import pandas as pd
from datetime import datetime
from typing import Dict, Any, Union, List

class ElectricityPredictor:
    """
    Production Machine Learning Predictor for Household Electricity Consumption.
    Supports authentic Karnataka district & month weather lookup, leak-free feature pipelines,
    and handles all 7 candidate regression model architectures.
    """

    def __init__(self, models_dir: str = None):
        if models_dir is None:
            base_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
            models_dir = os.path.join(base_dir, "models")

        self.models_dir = models_dir
        self.base_dir = os.path.dirname(models_dir)
        self.trained_dir = os.path.join(models_dir, "trained")
        self.preprocessing_dir = os.path.join(models_dir, "preprocessing")

        self.model = None
        self.preprocessor = None
        self.feature_schema = None
        self.target_metadata = None
        self.model_name = "Best Model"
        self.weather_lookup_df = None

        self._load_weather_data()
        self._load_artifacts()

    def _load_weather_data(self):
        weather_path = os.path.join(self.base_dir, "datasets", "weather", "karnataka_weather.csv")
        if os.path.exists(weather_path):
            try:
                self.weather_lookup_df = pd.read_csv(weather_path)
            except Exception as e:
                print(f"[!] Warning loading weather dataset: {e}")

    def _load_artifacts(self):
        best_model_path = os.path.join(self.trained_dir, "best_model.joblib")
        lstm_model_path = os.path.join(self.trained_dir, "lstm_model.joblib")
        xgb_model_path = os.path.join(self.trained_dir, "xgboost_model.joblib")

        model_path = best_model_path if os.path.exists(best_model_path) else (
            lstm_model_path if os.path.exists(lstm_model_path) else xgb_model_path
        )
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
        if "lstm" in model_type_str or "lstmnetwork" in model_type_str:
            self.model_name = "Long Short-Term Memory (LSTM) Deep Learning Regressor"
        elif "temporal" in model_type_str or "tft" in model_type_str:
            self.model_name = "Temporal Fusion Transformer (TFT) Deep Learning Regressor"
        elif "randomforest" in model_type_str:
            self.model_name = "Random Forest Regressor (Ensemble)"
        elif "xgb" in model_type_str:
            self.model_name = "XGBoost Regressor (Extreme Gradient Boosting)"
        elif "linear" in model_type_str:
            self.model_name = "Linear Regression (Ordinary Least Squares)"
        elif "decisiontree" in model_type_str:
            self.model_name = "Decision Tree Regressor"
        elif "kneighbor" in model_type_str:
            self.model_name = "K-Nearest Neighbors Regressor (KNN)"
        else:
            self.model_name = type(self.model).__name__

    def _lookup_weather_for_row(self, row: pd.Series) -> Dict[str, Any]:
        """
        Enriches input row with authentic Karnataka weather from karnataka_weather.csv
        based on district and target month (current 2026_11 or forward months up to 2027_10).
        """
        if self.weather_lookup_df is None or self.weather_lookup_df.empty:
            return {}

        district = str(row.get("district", "Bengaluru Urban")).strip()
        month_val = row.get("month", None)
        month_num = row.get("month_number", None)

        target_month_key = None
        if pd.notna(month_val) and "_" in str(month_val):
            target_month_key = str(month_val).strip()
        elif pd.notna(month_num):
            m = int(month_num)
            if m in [11, 12]:
                target_month_key = f"2026_{m:02d}"
            else:
                target_month_key = f"2027_{m:02d}"
        else:
            # Default to current month: November 2026
            target_month_key = "2026_11"

        # Search exact district match
        match = self.weather_lookup_df[
            (self.weather_lookup_df["district"].str.lower() == district.lower()) &
            (self.weather_lookup_df["month"] == target_month_key)
        ]

        if match.empty:
            # Fallback by district alone
            match = self.weather_lookup_df[self.weather_lookup_df["district"].str.lower() == district.lower()]

        if match.empty:
            # Fallback by month alone (statewide average for that month)
            match = self.weather_lookup_df[self.weather_lookup_df["month"] == target_month_key]

        if not match.empty:
            w = match.iloc[0]
            return {
                "district": district,
                "month_key": target_month_key,
                "mean_temperature_c": float(w["mean_temperature_c"]),
                "mean_max_temperature_c": float(w["mean_max_temperature_c"]),
                "mean_min_temperature_c": float(w["mean_min_temperature_c"]),
                "relative_humidity_percent": float(w["relative_humidity_percent"]),
                "rainfall_mm": float(w["rainfall_mm"])
            }
        return {}

    def _format_input(self, data: Union[Dict[str, Any], List[Dict[str, Any]], pd.DataFrame]) -> pd.DataFrame:
        if isinstance(data, dict):
            df = pd.DataFrame([data])
        elif isinstance(data, list):
            df = pd.DataFrame(data)
        elif isinstance(data, pd.DataFrame):
            df = data.copy()
        else:
            raise TypeError("Input must be a dict, list of dicts, or pandas DataFrame.")

        # Enrich with Karnataka authentic weather where applicable
        weather_applied_list = []
        for idx in range(len(df)):
            row = df.iloc[idx]
            w = self._lookup_weather_for_row(row)
            weather_applied_list.append(w)
            if w:
                # If weather was missing or using baseline defaults, use authentic data
                if "temperature" not in df.columns or pd.isna(df.loc[idx, "temperature"]) or df.loc[idx, "temperature"] == 28.5:
                    df.loc[idx, "temperature"] = w["mean_temperature_c"]
                if "mean_max_temperature_c" not in df.columns or pd.isna(df.loc[idx, "mean_max_temperature_c"]):
                    df.loc[idx, "mean_max_temperature_c"] = w["mean_max_temperature_c"]
                if "mean_min_temperature_c" not in df.columns or pd.isna(df.loc[idx, "mean_min_temperature_c"]):
                    df.loc[idx, "mean_min_temperature_c"] = w["mean_min_temperature_c"]
                if "humidity" not in df.columns or pd.isna(df.loc[idx, "humidity"]) or df.loc[idx, "humidity"] == 58.0:
                    df.loc[idx, "humidity"] = w["relative_humidity_percent"]
                if "rainfall" not in df.columns or pd.isna(df.loc[idx, "rainfall"]) or df.loc[idx, "rainfall"] == 45.0:
                    df.loc[idx, "rainfall"] = w["rainfall_mm"]

        self._last_weather_applied = weather_applied_list

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

        weather_info = self._last_weather_applied[0] if getattr(self, "_last_weather_applied", None) else {}

        return {
            "predicted_consumption_kwh": cleaned_preds[0] if len(cleaned_preds) == 1 else cleaned_preds,
            "predictions_list": cleaned_preds,
            "unit": "kWh",
            "model_algorithm": self.model_name,
            "sample_count": len(cleaned_preds),
            "weather_context": weather_info,
            "timestamp": datetime.now().isoformat(),
            "status": "SUCCESS"
        }

def predict_single_household(payload: Dict[str, Any], models_dir: str = None) -> Dict[str, Any]:
    predictor = ElectricityPredictor(models_dir=models_dir)
    return predictor.predict(payload)

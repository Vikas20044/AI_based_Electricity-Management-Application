"""
Feature Pipeline & Preprocessing Module
AI-Based Household Electricity Management Application

Ensures leak-free preprocessing, feature alignment between training and testing,
and preserves the exact feature schema required for future model inference (FastAPI & Spring Boot).
"""

import os
import json
import joblib
import pandas as pd
import numpy as np
from typing import Tuple, Dict, Any, List
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
from sklearn.preprocessing import StandardScaler, OneHotEncoder

TARGET_COLUMN: str = "monthly_consumption_kwh"

CATEGORICAL_FEATURES: List[str] = [
    "state",
    "district",
    "season",
    "dwelling_type"
]

NUMERICAL_FEATURES: List[str] = [
    "household_size",
    "appliance_count",
    "year",
    "month_number",
    "temperature",
    "mean_max_temperature_c",
    "mean_min_temperature_c",
    "temperature_range_c",
    "humidity",
    "rainfall",
    "has_air_conditioner",
    "has_air_cooler",
    "has_refrigerator",
    "has_washing_machine",
    "has_water_geyser",
    "has_water_pump",
    "has_television",
    "has_mixer_grinder",
    "lag_1_consumption",
    "lag_2_consumption",
    "previous_month_consumption",
    "rolling_average_consumption",
    "consumption_change_mom_kwh"
]

FEATURE_COLUMNS: List[str] = CATEGORICAL_FEATURES + NUMERICAL_FEATURES

LEAKAGE_COLUMNS: List[str] = [
    "gross_electricity_bill_inr",
    "electricity_bill_inr",
    "gruha_jyothi_eligible",
    "is_zero_bill",
    "effective_tariff_rate_inr",
    "per_capita_consumption_kwh"
]

METADATA_COLUMNS: List[str] = [
    "observation_id",
    "household_id",
    "location",
    "data_origin",
    "simulation_flag",
    "month_name"
]

def build_preprocessing_pipeline() -> ColumnTransformer:
    """
    Constructs a scikit-learn ColumnTransformer for numerical and categorical preprocessing.
    - Numerical: Median imputation + StandardScaler.
    - Categorical: Constant imputation ('Unknown') + OneHotEncoder (ignoring unseen categories).
    """
    num_pipeline = Pipeline([
        ("imputer", SimpleImputer(strategy="median")),
        ("scaler", StandardScaler())
    ])

    cat_pipeline = Pipeline([
        ("imputer", SimpleImputer(strategy="constant", fill_value="Unknown")),
        ("onehot", OneHotEncoder(handle_unknown="ignore", sparse_output=False))
    ])

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", num_pipeline, NUMERICAL_FEATURES),
            ("cat", cat_pipeline, CATEGORICAL_FEATURES)
        ],
        remainder="drop"
    )

    return preprocessor

def clean_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    """
    Cleans raw DataFrame by ensuring appropriate types, mapping inference aliases,
    and computing leak-free autoregressive past features.
    """
    df = df.copy()

    # Map aliases for inference compatibility
    if "temperature" not in df.columns and "mean_temperature_c" in df.columns:
        df["temperature"] = df["mean_temperature_c"]
    if "humidity" not in df.columns and "mean_relative_humidity_pct" in df.columns:
        df["humidity"] = df["mean_relative_humidity_pct"]
    if "rainfall" not in df.columns and "monthly_rainfall_mm" in df.columns:
        df["rainfall"] = df["monthly_rainfall_mm"]
    if "previous_month_consumption" not in df.columns and "previous_month_consumption_kwh" in df.columns:
        df["previous_month_consumption"] = df["previous_month_consumption_kwh"]
    if "lag_1_consumption" not in df.columns:
        df["lag_1_consumption"] = df.get("previous_month_consumption", df.get("previous_month_consumption_kwh", 100.0))
    if "lag_2_consumption" not in df.columns:
        df["lag_2_consumption"] = df.get("previous_2_month_consumption_kwh", df["lag_1_consumption"])
    if "previous_month_consumption" not in df.columns:
        df["previous_month_consumption"] = df["lag_1_consumption"]

    # Ensure leak-free past change and rolling average
    if "lag_1_consumption" in df.columns and "lag_2_consumption" in df.columns:
        df["consumption_change_mom_kwh"] = df["lag_1_consumption"] - df["lag_2_consumption"]
        df["rolling_average_consumption"] = (df["lag_1_consumption"] + df["lag_2_consumption"]) / 2.0

    # Ensure defaults for state, district, season, dwelling_type
    if "state" not in df.columns:
        df["state"] = "Karnataka"
    if "district" not in df.columns:
        df["district"] = "Bengaluru Urban"
    if "household_size" not in df.columns:
        df["household_size"] = 4.0
    if "dwelling_type" not in df.columns:
        df["dwelling_type"] = "2 BHK"
    if "year" not in df.columns:
        df["year"] = 2026
    if "month_number" not in df.columns:
        df["month_number"] = 11

    if "season" not in df.columns:
        def get_season(m):
            if m in [12, 1, 2]:
                return "Winter"
            elif m in [3, 4, 5]:
                return "Summer"
            elif m in [6, 7, 8, 9]:
                return "Monsoon"
            return "Post-monsoon"
        df["season"] = df["month_number"].apply(get_season)

    # Ensure weather range defaults
    if "temperature" not in df.columns:
        df["temperature"] = 26.5
    if "humidity" not in df.columns:
        df["humidity"] = 60.0
    if "rainfall" not in df.columns:
        df["rainfall"] = 45.0

    if "temperature_range_c" not in df.columns:
        t_max = df.get("mean_max_temperature_c", df.get("temperature", 30.0) + 4.0)
        t_min = df.get("mean_min_temperature_c", df.get("temperature", 25.0) - 4.0)
        df["mean_max_temperature_c"] = t_max
        df["mean_min_temperature_c"] = t_min
        df["temperature_range_c"] = t_max - t_min

    # Ensure appliance flags
    appliance_cols = [
        "has_air_conditioner", "has_air_cooler", "has_refrigerator",
        "has_washing_machine", "has_water_geyser", "has_water_pump",
        "has_television", "has_mixer_grinder"
    ]
    for ac in appliance_cols:
        if ac not in df.columns:
            df[ac] = 0.0
        else:
            df[ac] = df[ac].fillna(0.0).astype(float)

    if "appliance_count" not in df.columns:
        df["appliance_count"] = 5.0

    return df

def prepare_features(
    df: pd.DataFrame,
    is_training: bool = True
) -> Tuple[pd.DataFrame, Any]:
    """
    Extracts the feature matrix X and target vector y (if present).
    Validates presence of required features and guarantees no leakage columns enter X.
    """
    df_clean = clean_dataframe(df)

    missing_features = [f for f in FEATURE_COLUMNS if f not in df_clean.columns]
    if missing_features:
        raise ValueError(f"Missing required features in dataset: {missing_features}")

    X = df_clean[FEATURE_COLUMNS].copy()

    y = None
    if TARGET_COLUMN in df_clean.columns:
        y = df_clean[TARGET_COLUMN].copy()
    elif is_training:
        raise ValueError(f"Target column '{TARGET_COLUMN}' is missing from the training dataset.")

    return X, y

def save_preprocessing_artifacts(
    preprocessor: ColumnTransformer,
    output_dir: str,
    y_train: pd.Series = None
) -> Dict[str, str]:
    """
    Saves the fitted ColumnTransformer, feature schema, and target metadata
    into the specified directory for future inference services.
    """
    os.makedirs(output_dir, exist_ok=True)

    preprocessor_path = os.path.join(output_dir, "preprocessor.joblib")
    joblib.dump(preprocessor, preprocessor_path)

    feature_schema = {
        "target_variable": TARGET_COLUMN,
        "total_feature_count": len(FEATURE_COLUMNS),
        "categorical_features": CATEGORICAL_FEATURES,
        "numerical_features": NUMERICAL_FEATURES,
        "feature_order": FEATURE_COLUMNS,
        "excluded_leakage_columns": LEAKAGE_COLUMNS,
        "excluded_metadata_columns": METADATA_COLUMNS
    }
    schema_path = os.path.join(output_dir, "feature_schema.json")
    with open(schema_path, "w", encoding="utf-8") as f:
        json.dump(feature_schema, f, indent=4)

    target_metadata = {
        "name": TARGET_COLUMN,
        "unit": "kWh",
        "description": "End-of-month household electricity consumption in kWh",
        "train_statistics": {
            "count": int(y_train.count()) if y_train is not None else None,
            "mean": float(y_train.mean()) if y_train is not None else None,
            "std": float(y_train.std()) if y_train is not None else None,
            "min": float(y_train.min()) if y_train is not None else None,
            "median": float(y_train.median()) if y_train is not None else None,
            "max": float(y_train.max()) if y_train is not None else None
        } if y_train is not None else {}
    }
    target_path = os.path.join(output_dir, "target_metadata.json")
    with open(target_path, "w", encoding="utf-8") as f:
        json.dump(target_metadata, f, indent=4)

    return {
        "preprocessor_path": preprocessor_path,
        "schema_path": schema_path,
        "target_path": target_path
    }

def load_preprocessing_artifacts(artifact_dir: str) -> Tuple[ColumnTransformer, Dict[str, Any]]:
    """
    Loads the saved preprocessor and feature schema.
    """
    preprocessor_path = os.path.join(artifact_dir, "preprocessor.joblib")
    schema_path = os.path.join(artifact_dir, "feature_schema.json")

    if not os.path.exists(preprocessor_path):
        raise FileNotFoundError(f"Preprocessor file not found at: {preprocessor_path}")
    if not os.path.exists(schema_path):
        raise FileNotFoundError(f"Feature schema file not found at: {schema_path}")

    preprocessor = joblib.load(preprocessor_path)
    with open(schema_path, "r", encoding="utf-8") as f:
        schema = json.load(f)

    return preprocessor, schema

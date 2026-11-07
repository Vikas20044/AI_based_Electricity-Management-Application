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
    "area_type",
    "dwelling_type",
    "season"
]

NUMERICAL_FEATURES: List[str] = [
    "household_size",
    "large_household_flag",
    "year",
    "month_number",
    "days_in_month",
    "month_sin",
    "month_cos",
    "mean_temperature_c",
    "mean_relative_humidity_pct",
    "monthly_rainfall_mm",
    "previous_month_consumption_kwh",
    "previous_2_month_consumption_kwh",
    "previous_3_month_consumption_kwh",
    "previous_month_bill_inr",
    "previous_2_month_bill_inr",
    "previous_3_month_bill_inr",
    "prior_3_month_avg_consumption_kwh",
    "prior_3_month_avg_bill_inr",
    "prior_3_month_observation_count",
    "historical_mean_consumption_kwh",
    "historical_mean_bill_inr",
    "historical_observations_count",
    "lag1_vs_lag2_consumption_change_kwh",
    "lag1_vs_lag2_consumption_change_pct"
]

FEATURE_COLUMNS: List[str] = CATEGORICAL_FEATURES + NUMERICAL_FEATURES

LEAKAGE_COLUMNS: List[str] = [
    "estimated_bill_inr",
    "zero_consumption_flag",
    "high_consumption_flag",
    "bill_zero_usage_flag",
    "bill_per_kwh_original",
    "bill_ratio_review_flag",
    "consumption_imputed_flag",
    "daily_average_consumption_kwh",
    "consumption_per_person_kwh",
    "bill_per_kwh"
]

METADATA_COLUMNS: List[str] = [
    "household_id",
    "location",
    "month",
    "month_date"
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
    Cleans raw DataFrame by ensuring appropriate types for boolean and numeric columns.
    """
    df = df.copy()
    if "large_household_flag" in df.columns:
        df["large_household_flag"] = df["large_household_flag"].astype(int)
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

"""
Model Training Module
AI-Based Household Electricity Management Application

Trains five regression algorithms:
1. Linear Regression (Baseline)
2. Decision Tree Regression
3. Random Forest Regression
4. XGBoost Regression
5. K-Nearest Neighbors (KNN) Regression
"""

import os
import time
import joblib
import numpy as np
import pandas as pd
from typing import Dict, Any, Tuple
from sklearn.linear_model import LinearRegression
from sklearn.tree import DecisionTreeRegressor
from sklearn.ensemble import RandomForestRegressor
from sklearn.neighbors import KNeighborsRegressor
from xgboost import XGBRegressor

MODEL_FILENAMES = {
    "Linear Regression": "linear_regression_model.joblib",
    "Decision Tree": "decision_tree_model.joblib",
    "Random Forest": "random_forest_model.joblib",
    "XGBoost": "xgboost_model.joblib",
    "KNN": "knn_model.joblib"
}

def get_model_instances(random_state: int = 42) -> Dict[str, Any]:
    """
    Instantiates the five candidate regression models with standardized hyperparameters.
    """
    models = {
        "Linear Regression": LinearRegression(),
        "Decision Tree": DecisionTreeRegressor(
            random_state=random_state
        ),
        "Random Forest": RandomForestRegressor(
            n_estimators=100,
            random_state=random_state,
            n_jobs=-1
        ),
        "XGBoost": XGBRegressor(
            n_estimators=100,
            random_state=random_state,
            n_jobs=-1,
            eval_metric="rmse"
        ),
        "KNN": KNeighborsRegressor(
            n_neighbors=5,
            weights="uniform",
            n_jobs=-1
        )
    }
    return models

def train_single_model(
    name: str,
    model: Any,
    X_train: np.ndarray,
    y_train: pd.Series
) -> Tuple[Any, float]:
    """
    Fits an individual model and measures execution duration.
    """
    print(f"[*] Training {name}...")
    start_time = time.time()
    model.fit(X_train, y_train)
    elapsed = time.time() - start_time
    print(f"    -> Finished {name} in {elapsed:.2f} seconds.")
    return model, elapsed

def train_all_models(
    X_train_processed: np.ndarray,
    y_train: pd.Series,
    output_dir: str = None,
    random_state: int = 42
) -> Dict[str, Dict[str, Any]]:
    """
    Trains all five regression models and optionally saves each to the trained models directory.
    """
    model_instances = get_model_instances(random_state=random_state)
    trained_results = {}

    if output_dir:
        os.makedirs(output_dir, exist_ok=True)

    for name, model in model_instances.items():
        fitted_model, duration = train_single_model(name, model, X_train_processed, y_train)

        saved_path = None
        if output_dir:
            filename = MODEL_FILENAMES.get(name, f"{name.lower().replace(' ', '_')}_model.joblib")
            saved_path = os.path.join(output_dir, filename)
            joblib.dump(fitted_model, saved_path)
            print(f"    -> Saved {name} artifact to: {saved_path}")

        trained_results[name] = {
            "model": fitted_model,
            "training_time_seconds": duration,
            "artifact_path": saved_path
        }

    return trained_results

def save_trained_model(model: Any, model_name: str, output_dir: str) -> str:
    """
    Saves an individual model with the required standardized filename.
    """
    os.makedirs(output_dir, exist_ok=True)
    filename = MODEL_FILENAMES.get(model_name, f"{model_name.lower().replace(' ', '_')}_model.joblib")
    path = os.path.join(output_dir, filename)
    joblib.dump(model, path)
    return path

def save_best_model(model: Any, output_dir: str) -> str:
    """
    Saves the selected best-performing model as 'best_model.joblib'.
    """
    os.makedirs(output_dir, exist_ok=True)
    path = os.path.join(output_dir, "best_model.joblib")
    joblib.dump(model, path)
    print(f"[+] Successfully saved selected best model to: {path}")
    return path

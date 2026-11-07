"""
Model Evaluation Module
AI-Based Household Electricity Management Application

Computes MAE, MSE, RMSE, and R2 on training and testing datasets.
Generates comparative evaluation tables and identifies the best model.
"""

import os
import json
import numpy as np
import pandas as pd
from typing import Dict, Any, Tuple
from sklearn.metrics import mean_absolute_error, mean_squared_error, r2_score

def compute_metrics(y_true: pd.Series, y_pred: np.ndarray) -> Dict[str, float]:
    """
    Computes standard regression evaluation metrics.
    """
    mae = float(mean_absolute_error(y_true, y_pred))
    mse = float(mean_squared_error(y_true, y_pred))
    rmse = float(np.sqrt(mse))
    r2 = float(r2_score(y_true, y_pred))

    return {
        "MAE": round(mae, 4),
        "MSE": round(mse, 4),
        "RMSE": round(rmse, 4),
        "R2": round(r2, 4)
    }

def evaluate_all_models(
    trained_models: Dict[str, Any],
    X_train_processed: np.ndarray,
    y_train: pd.Series,
    X_test_processed: np.ndarray,
    y_test: pd.Series
) -> Tuple[pd.DataFrame, Dict[str, Any], Dict[str, np.ndarray]]:
    """
    Evaluates all trained models on both train and test splits.
    Returns:
    - comparison_df: Summary DataFrame
    - evaluation_records: Structured nested dictionary
    - predictions_dict: Dictionary containing test predictions for each model
    """
    records = []
    evaluation_records = {}
    predictions_dict = {}

    for name, item in trained_models.items():
        model = item["model"] if isinstance(item, dict) and "model" in item else item

        y_train_pred = model.predict(X_train_processed)
        y_test_pred = model.predict(X_test_processed)
        predictions_dict[name] = y_test_pred

        train_metrics = compute_metrics(y_train, y_train_pred)
        test_metrics = compute_metrics(y_test, y_test_pred)

        evaluation_records[name] = {
            "model_name": name,
            "train_metrics": train_metrics,
            "test_metrics": test_metrics,
            "training_time_seconds": item.get("training_time_seconds", None) if isinstance(item, dict) else None
        }

        records.append({
            "Model": name,
            "Train_MAE": train_metrics["MAE"],
            "Train_RMSE": train_metrics["RMSE"],
            "Train_R2": train_metrics["R2"],
            "Test_MAE": test_metrics["MAE"],
            "Test_MSE": test_metrics["MSE"],
            "Test_RMSE": test_metrics["RMSE"],
            "Test_R2": test_metrics["R2"]
        })

    comparison_df = pd.DataFrame(records)
    comparison_df = comparison_df.sort_values(by=["Test_R2", "Test_RMSE"], ascending=[False, True]).reset_index(drop=True)

    return comparison_df, evaluation_records, predictions_dict

def identify_best_model(comparison_df: pd.DataFrame) -> Tuple[str, Dict[str, Any]]:
    """
    Selects the best performing model based on highest Test R2 and lowest Test RMSE.
    """
    best_row = comparison_df.iloc[0]
    best_name = str(best_row["Model"])
    best_summary = best_row.to_dict()
    return best_name, best_summary

def save_evaluation_results(
    comparison_df: pd.DataFrame,
    evaluation_records: Dict[str, Any],
    metrics_dir: str,
    comparison_dir: str
) -> Dict[str, str]:
    """
    Saves individual model metrics and overall comparison tables as CSV and JSON.
    """
    os.makedirs(metrics_dir, exist_ok=True)
    os.makedirs(comparison_dir, exist_ok=True)

    comparison_csv = os.path.join(comparison_dir, "model_comparison.csv")
    comparison_json = os.path.join(comparison_dir, "model_comparison.json")
    comparison_df.to_csv(comparison_csv, index=False)

    with open(comparison_json, "w", encoding="utf-8") as f:
        json.dump(comparison_df.to_dict(orient="records"), f, indent=4)

    for name, rec in evaluation_records.items():
        sanitized_name = name.lower().replace(" ", "_")
        metric_file = os.path.join(metrics_dir, f"{sanitized_name}_metrics.json")
        with open(metric_file, "w", encoding="utf-8") as f:
            json.dump(rec, f, indent=4)

    all_metrics_json = os.path.join(metrics_dir, "all_models_metrics.json")
    with open(all_metrics_json, "w", encoding="utf-8") as f:
        json.dump(evaluation_records, f, indent=4)

    return {
        "comparison_csv": comparison_csv,
        "comparison_json": comparison_json,
        "all_metrics_json": all_metrics_json
    }

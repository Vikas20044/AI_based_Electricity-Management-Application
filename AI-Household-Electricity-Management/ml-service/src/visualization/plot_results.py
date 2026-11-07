"""
Visualization Module
AI-Based Household Electricity Management Application

Generates and saves publication-quality evaluation plots:
1. Model comparison metrics (MAE, RMSE, R2).
2. Actual vs. Predicted consumption scatter plots.
3. Prediction error (residual) distribution plots.
4. Feature importance / coefficient analysis.
"""

import os
from typing import Any, Dict, List, Tuple
os.environ.setdefault("MPLCONFIGDIR", os.path.join(os.path.expanduser("~"), "AppData", "Local", "Temp", "matplotlib"))
import numpy as np
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

def plot_model_comparison(comparison_df: pd.DataFrame, output_dir: str) -> str:
    """
    Creates bar charts comparing MAE, RMSE, and R2 across all five models.
    """
    os.makedirs(output_dir, exist_ok=True)
    models = comparison_df["Model"].tolist()
    r2_scores = comparison_df["Test_R2"].tolist()
    mae_scores = comparison_df["Test_MAE"].tolist()
    rmse_scores = comparison_df["Test_RMSE"].tolist()

    x = np.arange(len(models))
    width = 0.25

    fig, ax1 = plt.subplots(figsize=(15, 6))

    color_mae = "#2b5c8f"
    color_rmse = "#d95f02"
    color_r2 = "#2ca02c"

    rects1 = ax1.bar(x - width, mae_scores, width, label="Test MAE (kWh)", color=color_mae, alpha=0.85)
    rects2 = ax1.bar(x, rmse_scores, width, label="Test RMSE (kWh)", color=color_rmse, alpha=0.85)

    ax1.set_ylabel("Error Metric Value (kWh)", fontsize=12, fontweight="bold")
    ax1.set_xlabel("Regression Algorithm", fontsize=12, fontweight="bold")
    ax1.set_title("Machine Learning & Deep Learning Models: Performance Metrics Comparison on Test Set", fontsize=14, fontweight="bold", pad=15)
    ax1.set_xticks(x)
    ax1.set_xticklabels(models, fontsize=9, rotation=15)
    ax1.grid(axis="y", linestyle="--", alpha=0.4)

    ax2 = ax1.twinx()
    rects3 = ax2.bar(x + width, r2_scores, width, label="Test R² Score", color=color_r2, alpha=0.85)
    ax2.set_ylabel("R² Score (Coefficient of Determination)", fontsize=12, fontweight="bold", color=color_r2)
    ax2.tick_params(axis="y", labelcolor=color_r2)
    ax2.set_ylim(0, 1.0)

    for rect in rects1:
        h = rect.get_height()
        ax1.annotate(f"{h:.1f}", xy=(rect.get_x() + rect.get_width() / 2, h),
                     xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=8)
    for rect in rects2:
        h = rect.get_height()
        ax1.annotate(f"{h:.1f}", xy=(rect.get_x() + rect.get_width() / 2, h),
                     xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=8)
    for rect in rects3:
        h = rect.get_height()
        ax2.annotate(f"{h:.3f}", xy=(rect.get_x() + rect.get_width() / 2, h),
                     xytext=(0, 3), textcoords="offset points", ha="center", va="bottom", fontsize=8, color="darkgreen")

    lines1, labels1 = ax1.get_legend_handles_labels()
    lines2, labels2 = ax2.get_legend_handles_labels()
    ax1.legend(lines1 + lines2, labels1 + labels2, loc="upper right", framealpha=0.9)

    plt.tight_layout()
    output_path = os.path.join(output_dir, "model_comparison_metrics.png")
    plt.savefig(output_path, dpi=300)
    plt.close()
    return output_path

def plot_actual_vs_predicted(
    y_true: pd.Series,
    predictions_dict: dict,
    output_dir: str
) -> str:
    """
    Creates a grid of scatter plots showing Actual vs. Predicted values across all models.
    """
    os.makedirs(output_dir, exist_ok=True)
    model_names = list(predictions_dict.keys())
    n_models = len(model_names)
    n_cols = 3
    n_rows = int(np.ceil(n_models / n_cols))
    fig, axes = plt.subplots(n_rows, n_cols, figsize=(16, 4.5 * n_rows))
    axes = axes.flatten()

    colors = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"]

    max_val = max(float(y_true.max()), max([float(np.max(p)) for p in predictions_dict.values()]))

    for idx, name in enumerate(model_names):
        ax = axes[idx]
        y_pred = predictions_dict[name]

        ax.scatter(y_true, y_pred, alpha=0.45, color=colors[idx % len(colors)], edgecolors="none", s=25)
        ax.plot([0, max_val], [0, max_val], "r--", linewidth=1.5, label="Ideal Fit (y = x)")

        ax.set_title(f"{name}", fontsize=12, fontweight="bold")
        ax.set_xlabel("Actual Consumption (kWh)", fontsize=10)
        ax.set_ylabel("Predicted Consumption (kWh)", fontsize=10)
        ax.grid(True, linestyle="--", alpha=0.3)
        ax.legend(loc="upper left", fontsize=8)
        ax.set_xlim(0, max_val * 1.05)
        ax.set_ylim(0, max_val * 1.05)

    if len(model_names) < len(axes):
        for idx in range(len(model_names), len(axes)):
            fig.delaxes(axes[idx])

    fig.suptitle("Actual vs. Predicted Electricity Consumption (kWh) - Testing Dataset", fontsize=15, fontweight="bold")
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    output_path = os.path.join(output_dir, "actual_vs_predicted.png")
    plt.savefig(output_path, dpi=300)
    plt.close()
    return output_path

def plot_error_distribution(
    y_true: pd.Series,
    predictions_dict: dict,
    output_dir: str
) -> str:
    """
    Plots residual (error = Actual - Predicted) distribution histograms for all models.
    """
    os.makedirs(output_dir, exist_ok=True)
    model_names = list(predictions_dict.keys())
    n_models = len(model_names)
    n_cols = 3
    n_rows = int(np.ceil(n_models / n_cols))
    fig, axes = plt.subplots(n_rows, n_cols, figsize=(16, 4.5 * n_rows))
    axes = axes.flatten()

    colors = ["#1f77b4", "#ff7f0e", "#2ca02c", "#d62728", "#9467bd", "#8c564b", "#e377c2", "#7f7f7f", "#bcbd22", "#17becf"]

    for idx, name in enumerate(model_names):
        ax = axes[idx]
        residuals = y_true - predictions_dict[name]

        ax.hist(residuals, bins=40, color=colors[idx % len(colors)], alpha=0.75, edgecolor="black")
        ax.axvline(0, color="red", linestyle="--", linewidth=1.5, label="Zero Error")
        ax.axvline(float(residuals.mean()), color="darkblue", linestyle=":", linewidth=1.5, label=f"Mean: {residuals.mean():.1f}")

        ax.set_title(f"{name}: Error Distribution", fontsize=12, fontweight="bold")
        ax.set_xlabel("Residual Error (Actual - Predicted) [kWh]", fontsize=10)
        ax.set_ylabel("Frequency", fontsize=10)
        ax.grid(True, linestyle="--", alpha=0.3)
        ax.legend(loc="upper right", fontsize=8)

    if len(model_names) < len(axes):
        for idx in range(len(model_names), len(axes)):
            fig.delaxes(axes[idx])

    fig.suptitle("Prediction Residuals / Error Distributions (Testing Dataset)", fontsize=15, fontweight="bold")
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    output_path = os.path.join(output_dir, "prediction_error_distribution.png")
    plt.savefig(output_path, dpi=300)
    plt.close()
    return output_path

def plot_feature_importance(
    trained_models: dict,
    feature_names: list,
    output_dir: str,
    top_n: int = 15
) -> str:
    """
    Plots top feature importances for tree-based models and coefficients for Linear Regression.
    """
    os.makedirs(output_dir, exist_ok=True)
    fig, axes = plt.subplots(1, 3, figsize=(18, 6))

    rf_item = trained_models.get("Random Forest")
    rf_model = rf_item["model"] if isinstance(rf_item, dict) and "model" in rf_item else rf_item
    if rf_model and hasattr(rf_model, "feature_importances_"):
        rf_imp = pd.Series(rf_model.feature_importances_, index=feature_names).sort_values(ascending=False).head(top_n)
        axes[0].barh(rf_imp.index[::-1], rf_imp.values[::-1], color="#2ca02c", alpha=0.85)
        axes[0].set_title(f"Random Forest (Top {top_n} Features)", fontsize=11, fontweight="bold")
        axes[0].set_xlabel("Relative Importance")
        axes[0].grid(axis="x", linestyle="--", alpha=0.4)

    xgb_item = trained_models.get("XGBoost")
    xgb_model = xgb_item["model"] if isinstance(xgb_item, dict) and "model" in xgb_item else xgb_item
    if xgb_model and hasattr(xgb_model, "feature_importances_"):
        xgb_imp = pd.Series(xgb_model.feature_importances_, index=feature_names).sort_values(ascending=False).head(top_n)
        axes[1].barh(xgb_imp.index[::-1], xgb_imp.values[::-1], color="#d62728", alpha=0.85)
        axes[1].set_title(f"XGBoost (Top {top_n} Features)", fontsize=11, fontweight="bold")
        axes[1].set_xlabel("Relative Importance")
        axes[1].grid(axis="x", linestyle="--", alpha=0.4)

    lr_item = trained_models.get("Linear Regression")
    lr_model = lr_item["model"] if isinstance(lr_item, dict) and "model" in lr_item else lr_item
    if lr_model and hasattr(lr_model, "coef_"):
        lr_coef = pd.Series(np.abs(lr_model.coef_), index=feature_names).sort_values(ascending=False).head(top_n)
        axes[2].barh(lr_coef.index[::-1], lr_coef.values[::-1], color="#1f77b4", alpha=0.85)
        axes[2].set_title(f"Linear Regression (Top {top_n} |Coefficients|)", fontsize=11, fontweight="bold")
        axes[2].set_xlabel("Absolute Standardized Weight")
        axes[2].grid(axis="x", linestyle="--", alpha=0.4)

    fig.suptitle("Feature Importance & Coefficient Comparison Across Models", fontsize=14, fontweight="bold")
    plt.tight_layout(rect=[0, 0.03, 1, 0.95])
    output_path = os.path.join(output_dir, "feature_importance.png")
    plt.savefig(output_path, dpi=300)
    plt.close()
    return output_path

def generate_all_evaluation_plots(
    comparison_df: pd.DataFrame,
    y_test: pd.Series,
    predictions_dict: dict,
    trained_models: dict,
    preprocessor: Any,
    output_dir: str
) -> dict:
    """
    Coordinates creation and saving of all visual evaluation plots.
    """
    feature_names = []
    try:
        feature_names = preprocessor.get_feature_names_out().tolist()
    except Exception:
        feature_names = [f"f_{i}" for i in range(len(trained_models["Linear Regression"]["model"].coef_))]

    p1 = plot_model_comparison(comparison_df, output_dir)
    p2 = plot_actual_vs_predicted(y_test, predictions_dict, output_dir)
    p3 = plot_error_distribution(y_test, predictions_dict, output_dir)
    p4 = plot_feature_importance(trained_models, feature_names, output_dir)

    return {
        "model_comparison_plot": p1,
        "actual_vs_predicted_plot": p2,
        "error_distribution_plot": p3,
        "feature_importance_plot": p4
    }

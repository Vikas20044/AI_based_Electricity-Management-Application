"""
End-to-End Machine Learning Pipeline Runner
AI-Based Household Electricity Management Application

Executes the complete ML training, evaluation, comparison, artifact saving,
and sample inference verification pipeline.
"""

import os
import sys
import json
import numpy as np
import pandas as pd

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from src.data.load_data import load_datasets
from src.features.feature_pipeline import (
    build_preprocessing_pipeline,
    prepare_features,
    save_preprocessing_artifacts,
    TARGET_COLUMN,
    FEATURE_COLUMNS
)
from src.training.train_models import (
    train_all_models,
    save_best_model
)
from src.evaluation.evaluate_models import (
    evaluate_all_models,
    identify_best_model,
    save_evaluation_results
)
from src.visualization.plot_results import (
    generate_all_evaluation_plots
)
from src.inference.predict import (
    ElectricityPredictor
)

def run_pipeline():
    print("=" * 75)
    print("AI-BASED HOUSEHOLD ELECTRICITY MANAGEMENT APPLICATION")
    print("MACHINE LEARNING TRAINING & EVALUATION PIPELINE")
    print("=" * 75)

    datasets_dir = os.path.join(BASE_DIR, "datasets")
    models_dir = os.path.join(BASE_DIR, "models")
    trained_models_dir = os.path.join(models_dir, "trained")
    preprocessing_dir = os.path.join(models_dir, "preprocessing")
    results_dir = os.path.join(BASE_DIR, "evaluation-results")
    metrics_dir = os.path.join(results_dir, "metrics")
    comparison_dir = os.path.join(results_dir, "comparison")
    plots_dir = os.path.join(results_dir, "plots")

    print("\n[Step 1/7] Discovering and Loading Datasets...")
    train_df, test_df, files = load_datasets(BASE_DIR)

    print(f"\n[Step 2/7] Verifying Target Variable and Features...")
    if TARGET_COLUMN not in train_df.columns:
        raise ValueError(f"Target column '{TARGET_COLUMN}' not found in training dataset!")
    if TARGET_COLUMN not in test_df.columns:
        raise ValueError(f"Target column '{TARGET_COLUMN}' not found in testing dataset!")

    print(f"    -> Target Variable: '{TARGET_COLUMN}'")
    print(f"    -> Training target mean: {train_df[TARGET_COLUMN].mean():.2f} kWh, std: {train_df[TARGET_COLUMN].std():.2f} kWh")
    print(f"    -> Testing target mean:  {test_df[TARGET_COLUMN].mean():.2f} kWh, std: {test_df[TARGET_COLUMN].std():.2f} kWh")
    print(f"    -> Total Features Selected: {len(FEATURE_COLUMNS)}")

    print("\n[Step 3/7] Feature Extraction & Preprocessor Fitting (Preventing Data Leakage)...")
    X_train, y_train = prepare_features(train_df, is_training=True)
    X_test, y_test = prepare_features(test_df, is_training=False)

    preprocessor = build_preprocessing_pipeline()
    X_train_proc = preprocessor.fit_transform(X_train)
    X_test_proc = preprocessor.transform(X_test)

    print(f"    -> Transformed Training Feature Matrix Shape: {X_train_proc.shape}")
    print(f"    -> Transformed Testing Feature Matrix Shape:  {X_test_proc.shape}")

    print("\n[Step 4/7] Saving Preprocessing Artifacts & Feature Schema...")
    save_preprocessing_artifacts(preprocessor, preprocessing_dir, y_train=y_train)
    print(f"    -> Saved preprocessor to: {os.path.join(preprocessing_dir, 'preprocessor.joblib')}")
    print(f"    -> Saved schema to:       {os.path.join(preprocessing_dir, 'feature_schema.json')}")
    print(f"    -> Saved metadata to:     {os.path.join(preprocessing_dir, 'target_metadata.json')}")

    print("\n[Step 5/7] Training 7 Machine Learning & Deep Learning Regression Models...")
    trained_models = train_all_models(
        X_train_processed=X_train_proc,
        y_train=y_train,
        output_dir=trained_models_dir,
        random_state=42
    )

    print("\n[Step 6/7] Evaluating All 7 Models on Cleaned Testing Dataset...")
    comparison_df, eval_records, predictions_dict = evaluate_all_models(
        trained_models=trained_models,
        X_train_processed=X_train_proc,
        y_train=y_train,
        X_test_processed=X_test_proc,
        y_test=y_test
    )

    print("\n" + "=" * 80)
    print("COMPARATIVE MODEL PERFORMANCE TABLE (TESTING DATASET EVALUATION)")
    print("=" * 80)
    print(comparison_df.to_string(index=False))
    print("=" * 80)

    best_name, best_summary = identify_best_model(comparison_df)
    print(f"\n[+] Selected Best-Performing Model on Test Dataset: >>> {best_name} <<<")
    print(f"    Test R^2 Score: {best_summary['Test_R2']:.4f}")
    print(f"    Test RMSE:     {best_summary['Test_RMSE']:.4f} kWh")
    print(f"    Test MAE:      {best_summary['Test_MAE']:.4f} kWh")

    best_model_obj = trained_models[best_name]["model"]
    save_best_model(best_model_obj, trained_models_dir)

    save_evaluation_results(
        comparison_df=comparison_df,
        evaluation_records=eval_records,
        metrics_dir=metrics_dir,
        comparison_dir=comparison_dir
    )
    print(f"    -> Saved comparison metrics to: {comparison_dir}")
    print(f"    -> Saved individual metrics to: {metrics_dir}")

    print("\n[Step 7/7] Generating and Saving Evaluation Visualizations...")
    plot_paths = generate_all_evaluation_plots(
        comparison_df=comparison_df,
        y_test=y_test,
        predictions_dict=predictions_dict,
        trained_models=trained_models,
        preprocessor=preprocessor,
        output_dir=plots_dir
    )
    for plot_name, plot_path in plot_paths.items():
        print(f"    -> Generated {plot_name}: {plot_path}")

    print("\n" + "=" * 80)
    print("VERIFYING INFERENCE USING SAVED BEST MODEL ARTIFACT")
    print("=" * 80)
    predictor = ElectricityPredictor(models_dir=models_dir)

    sample_rows = test_df.head(2).to_dict(orient="records")
    for i, sample in enumerate(sample_rows, 1):
        actual_val = sample.get(TARGET_COLUMN, "N/A")
        hh_id = sample.get("household_id", f"Sample_{i}")
        result = predictor.predict(sample)
        pred_val = result["predicted_consumption_kwh"]
        print(f"Household: {hh_id}")
        print(f"  Actual Consumption:    {actual_val} kWh")
        print(f"  Predicted Consumption: {pred_val} kWh")
        print(f"  Model Used:            {result['model_algorithm']}")
        print(f"  Inference Status:      {result['status']}\n")

    print("[SUCCESS] Complete Machine Learning Pipeline Finished Successfully!")
    return comparison_df, best_name

if __name__ == "__main__":
    run_pipeline()

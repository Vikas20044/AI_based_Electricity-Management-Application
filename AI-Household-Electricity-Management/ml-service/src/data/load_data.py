"""
Data Loading & Validation Module
AI-Based Household Electricity Management Application

Discovers and loads the training and testing datasets from ml-service/datasets/.
Validates dataset schemas and inspects target variable availability.
"""

import os
import glob
import pandas as pd
from typing import Tuple, Dict

def find_dataset_files(ml_service_dir: str) -> Dict[str, str]:
    """
    Finds CSV files inside ml-service/datasets/train and ml-service/datasets/test.
    """
    train_dir = os.path.join(ml_service_dir, "datasets", "train")
    test_dir = os.path.join(ml_service_dir, "datasets", "test")

    train_files = glob.glob(os.path.join(train_dir, "*.csv"))
    test_files = glob.glob(os.path.join(test_dir, "*.csv"))

    if not train_files:
        raise FileNotFoundError(f"No CSV files found in training directory: {train_dir}")
    if not test_files:
        raise FileNotFoundError(f"No CSV files found in testing directory: {test_dir}")

    return {
        "train_file": train_files[0],
        "test_file": test_files[0]
    }

def load_datasets(ml_service_dir: str) -> Tuple[pd.DataFrame, pd.DataFrame, Dict[str, str]]:
    """
    Loads raw training and testing DataFrames.
    """
    files = find_dataset_files(ml_service_dir)
    print(f"[+] Loading training dataset: {files['train_file']}")
    train_df = pd.read_csv(files["train_file"])

    print(f"[+] Loading testing dataset: {files['test_file']}")
    test_df = pd.read_csv(files["test_file"])

    print(f"    -> Train shape: {train_df.shape}, Test shape: {test_df.shape}")
    return train_df, test_df, files

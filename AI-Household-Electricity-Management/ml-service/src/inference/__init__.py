"""
Inference Package
"""
from .predict import (
    ElectricityPredictor,
    predict_single_household
)

__all__ = [
    "ElectricityPredictor",
    "predict_single_household"
]

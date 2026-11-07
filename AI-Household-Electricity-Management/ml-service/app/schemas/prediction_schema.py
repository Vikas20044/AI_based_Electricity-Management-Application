from typing import Optional, List
from pydantic import BaseModel, Field

class PredictionRequest(BaseModel):
    state: str = Field(default="Karnataka")
    district: str = Field(default="Tumakuru")
    area_type: str = Field(default="Urban")
    dwelling_type: str = Field(default="Apartment/flat")
    season: str = Field(default="Summer/Pre-monsoon")

    household_size: int = Field(default=4, ge=1, le=20)
    large_household_flag: Optional[int] = Field(default=0)
    year: Optional[int] = Field(default=2024)
    month_number: Optional[int] = Field(default=5, ge=1, le=12)
    days_in_month: Optional[int] = Field(default=31, ge=28, le=31)
    month_sin: Optional[float] = Field(default=0.866)
    month_cos: Optional[float] = Field(default=-0.5)

    mean_temperature_c: float = Field(default=28.5)
    mean_relative_humidity_pct: float = Field(default=58.0)
    monthly_rainfall_mm: float = Field(default=45.0)

    previous_month_consumption_kwh: float = Field(default=170.0)
    previous_2_month_consumption_kwh: Optional[float] = Field(default=162.0)
    previous_3_month_consumption_kwh: Optional[float] = Field(default=155.0)
    previous_month_bill_inr: Optional[float] = Field(default=1200.0)
    previous_2_month_bill_inr: Optional[float] = Field(default=1150.0)
    previous_3_month_bill_inr: Optional[float] = Field(default=1100.0)
    prior_3_month_avg_consumption_kwh: Optional[float] = Field(default=162.3)
    prior_3_month_avg_bill_inr: Optional[float] = Field(default=1150.0)
    prior_3_month_observation_count: Optional[int] = Field(default=3)
    historical_mean_consumption_kwh: Optional[float] = Field(default=159.0)
    historical_mean_bill_inr: Optional[float] = Field(default=1130.0)
    historical_observations_count: Optional[int] = Field(default=12)
    lag1_vs_lag2_consumption_change_kwh: Optional[float] = Field(default=8.0)
    lag1_vs_lag2_consumption_change_pct: Optional[float] = Field(default=4.94)

    current_partial_consumption_kwh: Optional[float] = Field(default=None)
    days_elapsed: Optional[int] = Field(default=None, ge=1, le=31)

    sanctioned_load_kw: Optional[float] = Field(default=2.0)
    appliance_count: Optional[int] = Field(default=4)
    total_appliance_load_kw: Optional[float] = Field(default=1.5)
    appliance_monthly_kwh: Optional[float] = Field(default=140.0)
    heavy_appliance_flag: Optional[int] = Field(default=0)

class PredictionResponse(BaseModel):
    predicted_consumption_kwh: float
    unit: str = Field(default="kWh")
    model_algorithm: str
    model_version: str = Field(default="1.0.0")
    timestamp: str
    status: str = Field(default="SUCCESS")
    daily_run_rate_kwh: Optional[float] = Field(default=None)
    projected_remaining_kwh: Optional[float] = Field(default=None)

class ModelInfoResponse(BaseModel):
    active_model: str
    target_variable: str
    feature_count: int
    test_r2_score: float
    test_rmse_kwh: float
    features_list: List[str]

from fastapi import APIRouter, HTTPException, status
from app.schemas.prediction_schema import PredictionRequest, PredictionResponse, ModelInfoResponse
from app.services.ml_inference_service import inference_service

router = APIRouter(prefix="/api/v1", tags=["Machine Learning Inference"])

@router.post(
    "/predict",
    response_model=PredictionResponse,
    status_code=status.HTTP_200_OK,
    summary="Predict Monthly Electricity Consumption (kWh)"
)
async def predict_electricity_consumption(request: PredictionRequest):
    try:
        response = inference_service.predict(request)
        return response
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Inference pipeline execution error: {str(e)}"
        )

@router.get(
    "/features",
    response_model=ModelInfoResponse,
    status_code=status.HTTP_200_OK,
    summary="Retrieve Active Model Information & Feature Schema"
)
async def get_features_info():
    try:
        return inference_service.get_model_info()
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error retrieving model metadata: {str(e)}"
        )

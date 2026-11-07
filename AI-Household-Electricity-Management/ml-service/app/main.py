from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.api.prediction_router import router as prediction_router

app = FastAPI(
    title="Electricity Consumption ML Inference Service",
    description="Python FastAPI REST microservice serving pre-trained regression models for end-of-month electricity forecasting.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(prediction_router)

@app.get("/health", tags=["Health Check"])
async def health_check():
    return {
        "status": "UP",
        "service": "Electricity Consumption ML Inference Microservice",
        "framework": "FastAPI + Uvicorn",
        "active_model": "Linear Regression (Selected Best Model)"
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)

@echo off
echo =====================================================================
echo  AI-Based Household Electricity Management: Python ML Microservice
echo =====================================================================
cd /d "%~dp0"

set "PYTHON_CMD=python"
where python >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "%LOCALAPPDATA%\Programs\Python\Python313\python.exe" (
        set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python313\python.exe"
    )
)

echo Starting FastAPI ML Inference Server on port 8000...
"%PYTHON_CMD%" -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
pause

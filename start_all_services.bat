@echo off
echo =====================================================================
echo  AI-Based Household Electricity Management Application
echo  Master Service Launcher (MCA Major Project - Vikas K R)
echo =====================================================================
cd /d "%~dp0"

set "BASE_DIR=%~dp0"
if exist "%BASE_DIR%AI-Household-Electricity-Management" (
    set "PROJECT_DIR=%BASE_DIR%AI-Household-Electricity-Management\"
) else (
    set "PROJECT_DIR=%BASE_DIR%"
)

echo Project Directory: %PROJECT_DIR%

echo.
echo 1. Launching Python FastAPI ML Microservice (Port 8000)...
start "ML-Service-FastAPI (Port 8000)" /D "%PROJECT_DIR%ml-service" cmd /k run_ml_service.bat

echo 2. Launching Java Spring Boot Backend (Port 8080)...
start "Spring-Boot-Backend (Port 8080)" /D "%PROJECT_DIR%backend\electricity-management" cmd /k run_backend.bat

echo 3. Launching Web Frontend Server (Port 3000)...
start "Frontend-Server (Port 3000)" /D "%PROJECT_DIR%frontend" cmd /k run_frontend.bat

echo.
echo =====================================================================
echo  All 3 services are launching in separate persistent windows:
echo  - Frontend Web UI:      http://localhost:3000
echo  - Spring Boot API:      http://localhost:8080/api/v1/health
echo  - FastAPI ML API Docs:  http://localhost:8000/docs
echo =====================================================================
